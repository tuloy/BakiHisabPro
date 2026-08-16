# GitHub Setup — Baki Hisab Pro

1. Create a new GitHub repository, for example `BakiHisabPro`.
2. Extract this project ZIP.
3. Upload the **contents** of the extracted folder to the repository root.
4. Commit to `main`.
5. Open the repository's **Actions** tab.
6. The workflow **Build Android APK** will run automatically.
7. When it finishes, open the workflow run and download the `BakiHisabPro-debug-apk` artifact.

You can also manually start a build from Actions → Build Android APK → Run workflow.

For a public release APK, use a properly signed release build. The included workflow creates a debug APK for easy testing.
