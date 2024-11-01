if [ "$#" -ne 2 ]; then
  echo "Error: Exactly 2 parameters are required."
  echo "Usage: sh $0 envId imageVersion"
  exit 1
fi
if [[ -z "${GCLOUD_PROJECT_ID}" ]]; then
  echo "GCLOUD_PROJECT_ID env must be set"
  exit 1
fi

envId=$1
imageVersion=$2
projectId="${GCLOUD_PROJECT_ID}"
appName="productinfo"

rm -rf gitops

helm template $appName-release helm/$appName --output-dir gitops \
  --set env.id=$envId \
  --set image.version=$imageVersion \
  --set gcloud.projectId="$projectId" \
  --set gcloud.region="europe-west2"