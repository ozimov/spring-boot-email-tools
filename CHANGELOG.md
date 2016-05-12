# Change Log
All notable changes to Spring-Boot-Email-Tools project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Changed
- Module names to support generic template engines
- Added specific modules based on known template engines (Freemarker, Mustache and Pebble)


## [0.2.0] - 2016-03-12
### Added
- Added implementation of a scheduler service with class `PriorityQyeyeSchedulerService`

### Changed
- Using Spring Boot _1.3.4.RELEASE_
- Increased dependencies version
- Removed dependency `zalando/problem-spring-web` (you can handle exceptions as you prefer)

## [0.1.1] - 2016-03-09
### Fixed
- Minor bugfix for detecting mime type when not set for an email.

### Changed
- Increased dependencies version

### Added
- Added more unit test to increase coverage

## [0.1.0] - 2016-01-26
### Added
- Basic implementation with an `EmailService` that may use the Freemarker template engine .
