#!/usr/bin/env sh

set -eu

PATH_TO_CHECK="$1"

DIFF="$(git diff $(echo "$CIRCLE_COMPARE_URL" | sed -n -r -e 's|^.*/(\w+)\.\.\.(\w+)$|\1 \2|p') "$PATH_TO_CHECK")"

if [ -z "$DIFF" ]
then
    circleci step halt
fi