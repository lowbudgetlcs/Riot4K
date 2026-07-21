# Releasing Riot4K

Snapshots publish automatically on every merge to `main`; real releases are
cut by pushing a `v*` tag. Both require the one-time setup below.

## One-time setup

### 1. Maven Central namespace (requires DNS access)

1. Create an account at [central.sonatype.com](https://central.sonatype.com)
   and register the namespace `com.lowbudgetlcs`. The portal displays a
   **verification key**.
2. Add a `TXT` record on the **apex** domain `lowbudgetlcs.com` whose value is
   that key (the portal checks the apex only — not a subdomain). Create the
   record *before* clicking verify; a failed early attempt caches NXDOMAIN and
   delays retries.
3. Click **Verify** in the portal. The record may remain in DNS afterward.

If the domain is unavailable, the fallback is the `io.github.lowbudgetlcs`
namespace (verified with a temporary public repo, no DNS), which requires
changing the Gradle `group`.

### 2. Signing key

```sh
gpg --full-generate-key                  # RSA 4096; remember the passphrase
gpg --list-keys --keyid-format short    # note the 8-character key ID
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
gpg --export-secret-keys --armor <KEY_ID> > riot4k-signing.asc   # do NOT commit
```

### 3. Repository secrets

From the portal: **Account → Generate User Token** (this yields a token
username/password pair — not your login credentials). Then:

```sh
gh secret set MAVEN_CENTRAL_USERNAME   # token username
gh secret set MAVEN_CENTRAL_PASSWORD   # token password
gh secret set SIGNING_KEY_ID           # 8-character GPG key ID
gh secret set SIGNING_PASSWORD         # GPG key passphrase
gh secret set GPG_KEY_CONTENTS < riot4k-signing.asc
```

`RIOT_API_KEY` (for the weekly live-API canary) should already be set.

### 4. Docs custom domain (requires repo admin + DNS access)

The docs deploy to GitHub Pages on every merge. To serve them at
`docs.lowbudgetlcs.com`:

1. DNS: `CNAME` record `docs.lowbudgetlcs.com` → `lowbudgetlcs.github.io`.
2. Repo **Settings → Pages → Custom domain**: `docs.lowbudgetlcs.com`, then
   enable **Enforce HTTPS** once the certificate provisions (minutes to an
   hour).
3. Recommended: verify the domain for the org (**Org settings → Pages →
   Verified domains**), which adds a
   `_github-pages-challenge-lowbudgetlcs.lowbudgetlcs.com` TXT record with a
   code from that screen.

The generated site uses relative links, so it works unchanged at the domain
root.

## Cutting a release

1. Make sure `main` is green.
2. Tag it (the tag defines the version; `gradle.properties` stays `-SNAPSHOT`):

   ```sh
   git tag v0.1.0
   git push origin v0.1.0
   ```

   The publish workflow builds every target on macOS, signs the artifacts, and
   uploads the deployment to the Central Portal as version `0.1.0`.
3. In [central.sonatype.com](https://central.sonatype.com) → **Deployments**,
   review the validated deployment and click **Publish**. (This manual gate can
   be removed later by switching the build to automatic release.)
4. Artifacts appear on Maven Central within minutes; the public search index
   at search.maven.org follows within a couple of hours.
5. Optionally create a GitHub Release from the tag with release notes.
6. After the release, bump `version` in `gradle.properties` to the next
   `-SNAPSHOT` so main's snapshots move ahead of the release.

## Verifying a release

- `com.lowbudgetlcs:riot4k-api:<version>` resolves from
  `https://repo1.maven.org/maven2/com/lowbudgetlcs/`.
- The docs site reflects the release commit.
- Consumers on all platforms get the new version: the KMP metadata publishes
  jvm/android/ios/macos/linux/js variants from the single artifact set.

## Not yet wired (roadmap)

npm publishing for the JS package and Swift distribution (SPM
`binaryTarget` + Carthage via a checksummed XCFramework zip attached to GitHub
Releases) are tracked as the distribution-channels roadmap milestone.
