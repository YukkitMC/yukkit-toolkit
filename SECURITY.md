# Security Policy

Security reports help protect Yukkit Toolkit users. Please handle potential vulnerabilities responsibly.

## Supported versions

| Version | Security support |
| :--- | :--- |
| Development source | Best-effort review |
| Public releases | No stable release published yet |

There is currently no published security maintenance schedule or guaranteed response time. Future supported versions will be documented here once releases exist.

## Reporting a vulnerability

**Do not publish exploit steps, proof-of-concept code, or sensitive technical details in a public issue.**

1. Check whether the repository offers GitHub **Private vulnerability reporting** under its [Security tab](https://github.com/YukkitMC/yukkit-toolkit/security). If available, use that option.
2. If private reporting is not available, contact a project maintainer through an **official, private YukkitMC channel** before sharing technical details.
3. Include the affected version or commit, a concise impact assessment, reproduction steps, and a safe proof of concept if necessary.
4. Remove credentials, session data, and unrelated private information before sharing.

If no private channel is available, do not post sensitive vulnerability details publicly while seeking a safe reporting route.

## What to report

Examples include:

- Remote code execution or unsafe loading of external content.
- Credential, token, or personal-data exposure.
- Malicious or unexpected file and network access.
- Unsafe deserialization or path traversal.
- Distribution and build-chain compromise.

Normal game-server anti-cheat detections, server-rule enforcement, and feature requests are not security vulnerabilities by themselves.

## Coordinated handling

Maintainers may request more information, reproduce an issue, develop a fix, and coordinate disclosure. A report does not guarantee a bounty, a particular response time, or a public advisory.

Please avoid testing against third-party servers or other people's systems without authorization.

## Authentic releases

Official releases, when available, will be linked from the [repository Releases page](https://github.com/YukkitMC/yukkit-toolkit/releases). Treat JARs from unverified download mirrors as untrusted.
