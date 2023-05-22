def CHECKOUT_OUTPUT = ""

podTemplate(cloud: 'kubernetes', containers: [
  containerTemplate(args: '9999999', command: 'sleep', image: 'quay.io/buildah/stable:v1.23.1', 
    livenessProbe: containerLivenessProbe(execArgs: '', failureThreshold: 0, initialDelaySeconds: 0, periodSeconds: 0, successThreshold: 0, timeoutSeconds: 0),
    name: 'buildah', privileged: true, resourceLimitCpu: '', resourceLimitEphemeralStorage: '', resourceLimitMemory: '', resourceRequestCpu: '', resourceRequestEphemeralStorage: '', resourceRequestMemory: '', ttyEnabled: true, workingDir: '/home/jenkins/agent'), 
  containerTemplate(args: '9999999', command: 'sleep', image: 'bitnami/kubectl:1.27.1-debian-11-r3', 
    livenessProbe: containerLivenessProbe(execArgs: '', failureThreshold: 0, initialDelaySeconds: 0, periodSeconds: 0, successThreshold: 0, timeoutSeconds: 0), 
    name: 'kubectl', privileged: true, resourceLimitCpu: '', resourceLimitEphemeralStorage: '', resourceLimitMemory: '', resourceRequestCpu: '', resourceRequestEphemeralStorage: '', 
    resourceRequestMemory: '', ttyEnabled: true, workingDir: '/home/jenkins/agent')], serviceAccount: 'jenkins-agent') {

  node(POD_LABEL) {
    stage('Setup parameters'){
      script{
        properties([
          parameters([
            text(
              defaultValue: 'develop',
              name: 'BRANCH',
              describe:'Branch to build'
            )
          ])
        ])
      }
    }
    stage('Clone source'){
      CHECKOUT_OUTPUT = checkout scmGit(branches: [[name: '${BRANCH}']], extensions: [], userRemoteConfigs: [[credentialsId: 'gitlab-credential', url: 'http://gitlab-webservice-default.iamdat-service:8181/dattc/company.git']])
    }
    println "${CHECKOUT_OUTPUT['GIT_COMMIT'].substring(0,8)}"
    stage('Build') {
      container('buildah'){
        sh 'buildah build -t dattc/introcom company/'         
      }
    }
    stage('Login') {
      container('buildah'){
        withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW', usernameVariable: 'DOCKERHUB_CREDENTIALS_USR')]) {
          sh 'buildah login -u $DOCKERHUB_CREDENTIALS_USR -p $DOCKERHUB_CREDENTIALS_PSW docker.io' 
        }
      }
    }
    stage('Push') {
      container('buildah'){
        sh 'buildah push dattc/introcom'         
      }
    }
  }
}
