package com.ibm.devops.connect.SecuredActions;

import com.ibm.devops.connect.CloudItemListener;
import jenkins.model.Jenkins;
import hudson.model.AbstractItem;

import java.util.List;

public class BuildJobsList extends AbstractSecuredAction {

    protected void run(AbstractSecuredAction.ParamObj paramObj) {
        CloudItemListener cil = new CloudItemListener();
        cil.buildJobsList();
    }
}