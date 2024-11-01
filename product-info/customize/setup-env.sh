if [ "$#" -ne 1 ]; then
  echo "Error: Exactly 1 parameters are required."
  echo "Usage: sh $0 envId"
  exit 1
fi
if [[ -z "${GCLOUD_PROJECT_ID}" ]]; then
  echo "GCLOUD_PROJECT_ID env must be set"
  exit 1
fi
if [[ -z "${GCLOUD_PROJECT_NUMBER}" ]]; then
  echo "GCLOUD_PROJECT_NUMBER env must be set"
  exit 1
fi

envId=$1
projectId="${GCLOUD_PROJECT_ID}"
projectNumber="${GCLOUD_PROJECT_NUMBER}"
appName="productinfo"
sharedNamespace="hello"

### Creating Namespace ###
kubectl create namespace $envId || echo "Namespace already created"
kubectl label namespace $envId expose-apps=true --overwrite # Expose apps to gateway in other ns

### Copy Configmap ###
kubectl get configmap keycloakconfigmap --namespace=$sharedNamespace -o yaml | grep -v '^\s*namespace:\s' | kubectl apply --namespace=$envId -f -

### Copy Secrets ###
kubectl get secret mongosecret --namespace=$sharedNamespace -o yaml | grep -v '^\s*namespace:\s' | kubectl apply --namespace=$envId -f -

### Bind IAM Policy to K8S SA ###
gcloud projects add-iam-policy-binding projects/$projectId --role=roles/pubsub.editor --member=principal://iam.googleapis.com/projects/$projectNumber/locations/global/workloadIdentityPools/$projectId.svc.id.goog/subject/ns/$envId/sa/$appName --condition=None
