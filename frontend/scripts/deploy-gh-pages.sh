#!/usr/bin/env bash

set -exu

git config user.email "circleci-build-node@circleci.com"
git config user.name "CircleCI build node"

HASH_MESSAGE="$(git show --oneline | head -n1)"

GH_PAGES_DIR="~/gh-pages"

git clone git@github.com:CRogers/mealfu-frontend.git "${GH_PAGES_DIR}"

rm -rf "${GH_PAGES_DIR}/*"
cp -r site/* "${GH_PAGES_DIR}"

cd "${GH_PAGES_DIR}"
git add --all .

echo Remaining files:
git status
echo

git commit -m "https://github.com/CRogers/mealfu/commit/${HASH_MESSAGE} (deployed by ${CIRCLE_BUILD_URL}) [skip ci]"

git push origin master