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
appName="merchant"

### Delete resources ###
sh delete-gitops.sh $envId

### Remove IAM Policy to K8S SA ###
gcloud projects remove-iam-policy-binding projects/$projectId --role=roles/pubsub.editor --member=principal://iam.googleapis.com/projects/$projectNumber/locations/global/workloadIdentityPools/$projectId.svc.id.goog/subject/ns/$envId/sa/$appName --condition=None

echo "ConfigMap from shared namespace are not delete"
echo "Secret from shared namespace are not delete"
echo "Namespace is not delete"