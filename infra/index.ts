import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";
import * as awsx from "@pulumi/awsx";

let config = new pulumi.Config();
let serviceVersion = config.require("service-version");
console.log("Service version: " + serviceVersion);

serviceVersion = serviceVersion ? serviceVersion : "latest";

const repo = new awsx.ecr.Repository("fayi/my-ecr-repo");
const listener = new awsx.elasticloadbalancingv2.NetworkListener("my-lb", {
  port: 80,
});

const img = new awsx.ecr.RepositoryImage(
  repo,
  pulumi.interpolate`${repo.repository.repositoryUrl}:${serviceVersion}`
);

const service = new awsx.ecs.FargateService("svc", {
  desiredCount: 1,
  taskDefinitionArgs: {
    container: {
      image: img,
      cpu: 512,
      memory: 128,
      essential: true,
      portMappings: [listener],
    },
  },
});

export const repoUrn = repo.urn;
export const frontendUrl = pulumi.interpolate`http://${listener.endpoint.hostname}`;
