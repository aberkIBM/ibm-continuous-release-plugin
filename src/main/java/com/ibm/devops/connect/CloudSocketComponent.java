/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2017. All Rights Reserved.
 *
 * Note to U.S. Government Users Restricted Rights:  Use,
 * duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp.
 *******************************************************************************/
package com.ibm.devops.connect;

import java.net.URI;
import java.util.Properties;

import jenkins.model.Jenkins;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.devops.connect.DevOpsGlobalConfiguration;

import com.ibm.cloud.urbancode.connect.client.ConnectSocket;
import com.ibm.cloud.urbancode.connect.client.Listeners;
import com.ibm.devops.connect.OnConnectListener;

import com.ibm.devops.connect.CloudPublisher;

import io.socket.client.Socket;
import com.ibm.devops.connect.SecuredActions.BuildJobsList;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.*;

import java.io.IOException;
import com.ibm.devops.connect.Endpoints.EndpointManager;

import java.net.MalformedURLException;

public class CloudSocketComponent {

    public static final Logger log = LoggerFactory.getLogger(CloudSocketComponent.class);
    private String logPrefix= "[IBM Cloud DevOps] CloudSocketComponent#";

    final private IWorkListener workListener;
    final private String cloudUrl;
    private ConnectSocket socket;

    private static boolean otherIntegrationExists = false;

    private static void setOtherIntegrationsExists(boolean exists) {
        otherIntegrationExists = exists;
    }

    public CloudSocketComponent(IWorkListener workListener, String cloudUrl) {
        this.workListener = workListener;
        this.cloudUrl = cloudUrl;
    }

    public boolean isRegistered() {
        return StringUtils.isNotBlank(getSyncToken());
    }

    public String getSyncId() {
    	return Jenkins.getInstance().getDescriptorByType(DevOpsGlobalConfiguration.class).getSyncId();
    }

    public String getSyncToken() {
    	return Jenkins.getInstance().getDescriptorByType(DevOpsGlobalConfiguration.class).getSyncToken();
    }

    public void connectToCloudServices() throws Exception {
    	logPrefix= logPrefix + "connectToCloudServices ";
        String syncId = getSyncId();
        String syncToken = getSyncToken();

        CloudPublisher cloudPublisher = new CloudPublisher();

        boolean shouldConnect = true;

        if(shouldConnect) {
            ConnectionFactory factory = new ConnectionFactory();
            EndpointManager em = new EndpointManager();

            // Public Jenkins Client Credentials
            factory.setUsername("jenkins");
            factory.setPassword("jenkins");
            String hostname = em.getVelocityHostname();
            factory.setHost(hostname);
            factory.setPort(5672);

            Connection conn = factory.newConnection();

            Channel channel = conn.createChannel();

            log.info("Connecting to RabbitMQ");

            String EXCHANGE_NAME = "jenkins";
            String queueName = "jenkins.client." + syncId;

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                            AMQP.BasicProperties properties, byte[] body) throws IOException {

                    if (envelope.getRoutingKey().contains(".heartbeat")) {
                        CloudPublisher cloudPublisher = new CloudPublisher();
                        String syncId = getSyncId();
                        String syncToken = getSyncToken();

                        EndpointManager em = new EndpointManager();
                        
                        String url = removeTrailingSlash(Jenkins.getInstance().getDescriptorByType(DevOpsGlobalConfiguration.class).getBaseUrl());
                        boolean connected = cloudPublisher.testConnection(syncId, syncToken, url);
                    } else {
                        String message = new String(body, "UTF-8");
                        System.out.println(" [x] Received '" + message + "'");

                        CloudWorkListener2 cloudWorkListener = new CloudWorkListener2();
                        cloudWorkListener.call("startJob", message);
                    }
                }
            };

            channel.basicConsume(queueName, true, consumer);

            log.info(logPrefix + "\n\n\tAbout to attempt building list...\n\n");

            BuildJobsList buildJobList = new BuildJobsList();
            buildJobList.runAsJenkinsUser(null);
        }
    }

    private String removeTrailingSlash(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    // this does get called, but you may not see logging in the console. it will appear in the file.
    public void disconnect() {
        if (socket != null) {
            try {
                socket.disconnect();
                log.info(logPrefix + "Disconnected from the cloud service");
            }
            catch (Exception e) {
                log.error(logPrefix + "Error disconnecting the cloud service gracefully", e);
            }
            finally {
                socket = null;
            }
        }
    }

    public boolean connected() {
        if(socket == null) {
            return false;
        }
        return socket.connected();
    }

    public String getCauseOfFailure() {
        if (otherIntegrationExists) {
            return "These credentials have been used by another Jenkins Instance.  Please generate another Sync Id and provide those credentials here.";
        }

        return null;
    }
}
