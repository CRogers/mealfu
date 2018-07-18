#!/usr/bin/env sh

set -eu

PATHS_TO_CHECK="$@"

DIFF="$(git diff $(echo "${CIRCLE_COMPARE_URL}" | sed -n -r -e 's|^.*/(\w+)\.\.\.(\w+)$|\1 \2|p') "${PATHS_TO_CHECK}")"

if [ -z "${DIFF}" ]
then
    circleci step halt
fi