# Privacy Policy for Age Signal Checker

_Last updated: July 28, 2026_

This Privacy Policy describes how the **Age Signal Checker** app (package name
`com.baijiahu.test.age.signal`, the "App") handles information. The App is a developer
tool for testing the Google Play Age Signals API. Your privacy is important to us, and
this policy explains our practices in plain language.

## 1. Information We Collect

The App **does not collect, store, or share any personal or sensitive user data**. There
is no account to create, and the App does not gather names, email addresses, contacts,
location, or device identifiers for our own purposes.

## 2. Age Signals API

The App's core function is to call the Google Play Age Signals API (version 0.0.4) and
display the result on your device. It exposes two actions:

- **Request age signals access** — invokes `requestAgeSignalsAccess()`, which may launch
  a Google Play consent flow, and displays the returned age signals status.
- **Check age signals** — invokes `checkAgeSignals()` and displays the returned values
  (age range source, significant change status, significant change approval date, age
  lower/upper bounds, and install ID).

These requests are handled entirely by Google Play services on your device. The values
returned are shown **on-screen only**. The App does not transmit them to us or to any
third party, and it keeps no copy of these values after you close it.

Google's handling of these requests is governed by the
[Google Privacy Policy](https://policies.google.com/privacy).

## 3. Advertising and Analytics

The App contains **no advertising**, **no in-app purchases**, and **no third-party
analytics or tracking SDKs**.

## 4. Data Sharing

Because the App does not collect data, it does not sell or share any user data with third
parties.

## 5. Children's Privacy

The App does not knowingly collect any information from children or any other users.

## 6. Changes to This Policy

We may update this Privacy Policy from time to time. Any changes will be posted on this
page with an updated "Last updated" date.

## 7. Contact

If you have any questions about this Privacy Policy, contact us at:
stoning@gmail.com
