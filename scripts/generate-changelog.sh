#!/usr/bin/env sh

VERSION=$(grep scm.tag= release.properties | awk -Fv '{print $2}')
echo "Generating changelog for version $VERSION"

# Remove existing unreleased section (everything from '## [unreleased]' to the
# next version header) so it doesn't duplicate the new version section.
sed -i.bak '/^## \[unreleased\]/,/^## \[/{/^## \[unreleased\]/d;/^## \[/!d;}' CHANGELOG.md
rm -f CHANGELOG.md.bak

# Prepend new version section to existing changelog. This preserves all
# historical sections (with their corrected issue/commit links) and only adds
# the new release at the top.
git cliff --unreleased --tag "v$VERSION" --prepend CHANGELOG.md

git add CHANGELOG.md
