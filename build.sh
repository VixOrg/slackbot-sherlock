#!/usr/bin/env bash
echo 'Build, tag and push Docker image utility'

finologeeImage=finologee/slackbot-sherlock
ecrRepo=094983160805.dkr.ecr.eu-central-1.amazonaws.com
version=latest
push=n
buildDocs=n
dryRun=n

while [[ $# -gt 0 ]]; do
  key="$1"

  case $key in
  -v | --version)
    version="$2"
    shift 2 # past argument and value
    ;;
  -g | --git-commit)
    version=$(git log --pretty=format:'%h' -n 1)
    shift
    ;;
  -p | --push)
    push=y
    shift
    ;;
  --dry-run)
    dryRun=y
    shift
    ;;
  -d | --docs)
    buildDocs=y
    shift
    ;;
  *) # unknown option
    shift
    ;;
  esac
done

buildImageCommand="docker build --no-cache -t $finologeeImage:latest ."
tagImageCommand="docker tag $finologeeImage:latest $ecrRepo/$finologeeImage:$version"
buildDocImageCommand="docker build --no-cache -t $finologeeImageDocs:latest -f Dockerfile-documentation ."
tagDocImageCommand="docker tag $finologeeImageDocs:latest $ecrRepo/$finologeeImageDocs:$version"
pushImageCommand="docker push $ecrRepo/$finologeeImage:$version"
pushDocImageCommand="docker push $ecrRepo/$finologeeImageDocs:$version"

if [ $dryRun = 'y' ]; then
  echo "$buildImageCommand"

  if [ $version != 'latest' ]; then
    echo "$tagImageCommand"
  fi

  if [ $buildDocs = 'y' ]; then
    echo "$buildDocImageCommand"

    if [ $version != 'latest' ]; then
      echo "$tagDocImageCommand"
    fi
  fi

  if [ $push = 'y' ]; then
    echo 'AWS ECR Login'

    echo "$pushImageCommand"

    if [ $buildDocs = 'y' ]; then
      echo "$pushDocImageCommand"
    fi
  fi
else
  $buildImageCommand

  if [ $version != 'latest' ]; then
    $tagImageCommand
  fi

  if [ $buildDocs = 'y' ]; then
    $buildDocImageCommand

    if [ $version != 'latest' ]; then
      $tagDocImageCommand
    fi
  fi

  if [ $push = 'y' ]; then
    echo 'AWS ECR Login'
    awsLogin=$(aws ecr get-login --no-include-email)
    $awsLogin

    $pushImageCommand

    if [ $buildDocs = 'y' ]; then
      $pushDocImageCommand
    fi
  fi
fi
