## 1.2.2

* Keep SMS sent/delivered callbacks inside the host app and give each send a
  unique session-scoped, single-use callback identity, preventing other apps
  or previous app sessions from forging status.
* Give each send or query permission request its own code, and complete and
  remove listeners exactly once so later grants cannot replay older requests.
* Validate a single recipient in web SMS URIs.
* Correct Android source and iOS registration/codec defects, and align the
  platform-channel test codec.

## 1.1.0

* Upgraded gradle

## 1.0.2

* A fix on ios build by @eslamodeh
* 
## 1.0.1

* Added Web implementation

## 1.0.0

* Added IOS implementation

## 0.0.1

* TODO: Describe initial release.
