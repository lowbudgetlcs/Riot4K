import { spawn, type ChildProcess } from "node:child_process";
import { fileURLToPath } from "node:url";
import path from "node:path";
import type { TestProject } from "vitest/node";

declare module "vitest" {
  export interface ProvidedContext {
    mockPort: number;
  }
}

const samplesDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../..");
const serverBin = path.join(
  samplesDir,
  "mock-riot-server/build/install/mock-riot-server/bin/mock-riot-server",
);

function waitForPort(child: ChildProcess): Promise<number> {
  return new Promise((resolve, reject) => {
    let output = "";
    const timer = setTimeout(
      () => reject(new Error(`mock server did not report a port; output so far:\n${output}`)),
      30_000,
    );
    child.stdout?.on("data", (chunk: Buffer) => {
      output += chunk.toString();
      const match = output.match(/MOCK_RIOT_SERVER_PORT=(\d+)/);
      if (match) {
        clearTimeout(timer);
        resolve(Number(match[1]));
      }
    });
    child.on("error", reject);
    child.on("exit", (code) =>
      reject(new Error(`mock server exited early (code ${code}); output:\n${output}`)),
    );
  });
}

/**
 * Starts the shared mock Riot server (built by `:mock-riot-server:installDist`)
 * before the suite and stops it after; tests read the port via inject("mockPort").
 */
export default async function setup(project: TestProject): Promise<() => void> {
  const child = spawn(serverBin, ["--port=0"], {
    env: { ...process.env, FIXTURES_DIR: path.join(samplesDir, "fixtures") },
  });
  const port = await waitForPort(child);
  project.provide("mockPort", port);
  return () => {
    child.kill();
  };
}
