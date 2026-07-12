// swift-tools-version:5.9
import PackageDescription

// Consumes the locally built Riot4K XCFramework. Build it first:
//   ./gradlew :riot4k-api:assembleRiot4KSDKReleaseXCFramework
let package = Package(
    name: "RiotAccountApp",
    platforms: [
        .macOS(.v13),
        .iOS(.v14),
    ],
    targets: [
        .binaryTarget(
            name: "Riot4KSDK",
            path: "../../riot4k-api/build/XCFrameworks/release/Riot4KSDK.xcframework"
        ),
        .target(
            name: "RiotAccountApp",
            dependencies: ["Riot4KSDK"]
        ),
        .testTarget(
            name: "RiotAccountAppTests",
            dependencies: ["RiotAccountApp"]
        ),
    ]
)
