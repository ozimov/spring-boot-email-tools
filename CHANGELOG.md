# Change Log
All notable changes to Spring-Boot-Email-Tools project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

## [0.3.7] - 2017-01-09
### Fixed
- Support to email attachment has been fixed by removing the content type of attachments (issue 15)

## [0.3.7] - 2017-01-09
### Fixed
- Support to email attachment has been fixed by removing the content type of attachments (issue 15)
### Changed
- Removed Apache Tika dependency

## [0.3.6] - 2017-01-05
### Changed
- Using Spring Boot _1.4.3.RELEASE_
### Fixed
- Support to email attachment has been fixed (issue 15)

## [0.3.5] - 2017-01-04
### Added
- Added receipt notification in email header

## [0.3.4] - 2016-07-05
### Changed
- Using Spring Boot _1.3.6.RELEASE_

## [0.3.3] - 2016-07-01
### Fixed
- Corrected annotation for autowiring beans in `README.md`
- Made `TemplateService` optional in `EmailService`

## [0.3.2] - 2016-05-26
### Added
- Added support for **Thymeleaf** (version from Spring Boot _1.3.5.RELEASE_)

## [0.3.1] - 2016-05-14
### Changed
- Using Spring Boot _1.3.5.RELEASE_

## [0.3.0] - 2016-05-14
### Added
- Added support for **Mustache** (version from Spring Boot _1.3.4.RELEASE_)
  and **Pebble** (_version 2.2.1_)

### Changed
- Main module changed to `spring-boot-email-core` to support generic template engines
- Added specific modules based on known template engines (Freemarker, Mustache and Pebble)

### Fixed
- Closing mechanism of the scheduler is now responsive

## [0.2.0] - 2016-05-12
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
