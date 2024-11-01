if [ "$#" -ne 3 ]; then
  echo "Error: Exactly 3 parameters are required."
  echo "Usage: sh $0 envId localVersion remoteVersion"
  exit 1
fi

envId=$1
localVersion=$2
remoteVersion=$3

sh ./build-image.sh

sh ./push-image.sh $localVersion $remoteVersion

sh ./gen-gitops.sh $envId $remoteVersion

sh ./setup-env.sh $envId

sh ./apply-gitops.sh $envId