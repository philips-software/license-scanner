# Contributing to License Scanner 

 - [Question or Problem?](#question)
 - [Issues and Bugs](#issue)
 - [Releasing](#release)

## <a name="question"></a> Got a Question or Problem?
Please raise an issue for now. We don't have other official communication channels in place right now. If you really want to know more, you can contact the contributors through the standard Philips communication channel.

## <a name="issue"></a> Found an Issue?
If you find a bug in the source code or a mistake in the documentation, you can help us by submitting an issue to our [Github Repository][github]. Even better you can submit a Pull Request with a fix.

## <a name="release"></a> Create a release?
We're using gitflow to release. In versioning we're using a prefix `v` for example: `v0.2.1`.

### Initial setup
You need setup git flow once on your local machine:

```
git flow init
```

### Make sure your local environment is correct
```
git checkout main
git pull origin main
git checkout develop
git pull origin develop
```

### Start a release
```
git flow release start vx.x.x
```

### Change versions on various places
This needs to be improved in the future, but for now:

Change version into new version in file / linenumber:
- `.spdx-builder.yml` : line 9.
- `build.gradle` : line 81.
- `.github/workflows/gradle.yml` : line 94. (docker tags)
- `docker-compose.yml` : line 5.

Commit changes:
```
git commit -m "Prepare for release vx.x.x"
```

### Finish a release
```
git flow release finish vx.x.x
git push origin develop
git checkout main
git push origin main --tags
```

[github]: https://github.com/philips-software/license-scanner/issues

