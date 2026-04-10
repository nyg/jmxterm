#!/usr/bin/env sh

VERSION=$(grep scm.tag= release.properties | awk -Fv '{print $2}')
echo "Generating changelog for version $VERSION"

git cliff -o CHANGELOG.md --tag $VERSION
git add CHANGELOG.md
