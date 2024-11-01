if [ "$#" -ne 1 ]; then
  echo "Error: Exactly 1 parameters are required."
  echo "Usage: sh $0 envId"
  exit 1
fi

envId=$1
appName="merchant"

kubectl delete -f gitops/$appName/templates -n $envId