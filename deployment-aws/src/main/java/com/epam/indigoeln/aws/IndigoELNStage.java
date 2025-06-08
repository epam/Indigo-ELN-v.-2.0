package com.epam.indigoeln.aws;

import software.amazon.awscdk.Aspects;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.iam.CfnRole;
import software.constructs.Construct;

public class IndigoELNStage extends Stage {

    public IndigoELNStage(Construct scope, String id, software.amazon.awscdk.Environment env) {
        super(scope, id, StageProps.builder().env(env).build());

        new MainStack(this, "main-stack", new MainStack.Props(
                id
        ));

        Tags.of(this).add("project", "IndigoELN");
        Tags.of(this).add("stage", id);

        String permissionBoundary = StageParameters.load(id).getPermissionBoundary();

        Aspects.of(this).add(node -> {
//                for (IConstruct child : node.getNode().getChildren()) {
//                    System.out.println("\t!!! child " + child.getClass().getName() + " : " + child);
//                    if (child instanceof CfnSecurityGroupIngress sgi && sgi.getDescription() != null && sgi.getDescription().contains("Proxy")) {
//                            Boolean result = node.getNode().tryRemoveChild(child.getNode().getId());
//                            System.out.println("\t\t!!! removed: " + result);
//                    }
//                    if (child instanceof CfnSecurityGroupEgress sge && sge.getDescription() != null && sge.getDescription().contains("Proxy")) {
//                            Boolean result = node.getNode().tryRemoveChild(child.getNode().getId());
//                            System.out.println("\t\t!!! removed: " + result);
//                    }
//                }
            if (node instanceof SecurityGroup sg) {
//                    System.out.println("!!! " + node.getClass().getName() + " : " + node);
            }
            if (node instanceof CfnRole role && role.getPermissionsBoundary() == null && permissionBoundary != null) {
                role.setPermissionsBoundary(permissionBoundary);
            }
        });
    }
}
