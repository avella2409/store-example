if [ "$#" -ne 2 ]; then
  echo "Error: Exactly 2 parameters are required."
  echo "Usage: sh $0 localVersion remoteVersion"
  exit 1
fi
if [[ -z "${GCLOUD_PROJECT_ID}" ]]; then
  echo "GCLOUD_PROJECT_ID env must be set"
  exit 1
fi

localVersion=$1
remoteVersion=$2

projectId="${GCLOUD_PROJECT_ID}"
appName="productinfo"
repository="helloworld"
region="europe-west2"

docker tag $appName:$localVersion $region-docker.pkg.dev/$projectId/$repository/$appName:$remoteVersion

docker push $region-docker.pkg.dev/$projectId/$repository/$appName:$remoteVersion