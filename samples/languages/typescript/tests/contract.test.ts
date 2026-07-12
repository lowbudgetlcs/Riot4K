import { afterEach, beforeEach, expect, inject, test } from "vitest";
import { Riot4KJs } from "riot4k-api";
import { AccountRepository } from "../src/accountRepository.js";

/**
 * Contract tests: the TypeScript binding exercised end-to-end through the real
 * npm distribution and Node HTTP engine against the shared mock Riot server.
 */
let riot4k: Riot4KJs;
let repository: AccountRepository;

beforeEach(() => {
  riot4k = new Riot4KJs("test-key", `http://127.0.0.1:${inject("mockPort")}`, 0);
  repository = new AccountRepository(riot4k);
});

afterEach(() => {
  riot4k.close();
});

test("successful lookup maps to found with echoed riot ID", async () => {
  const lookup = await repository.lookup("AMERICAS", "Hide on bush", "KR1");
  expect(lookup).toMatchObject({ kind: "found", riotId: "Hide on bush#KR1" });
  if (lookup.kind === "found") {
    expect(lookup.puuid).toMatch(/^mock-puuid-/);
  }
});

test("unknown riot ID maps to missing, not an error", async () => {
  const lookup = await repository.lookup("AMERICAS", "NotFound", "x");
  expect(lookup).toEqual({ kind: "missing" });
});

test("API failure maps to a typed error with the status", async () => {
  const lookup = await repository.lookup("AMERICAS", "ServerError", "x");
  expect(lookup).toMatchObject({ kind: "error" });
  if (lookup.kind === "error") {
    expect(lookup.message).toContain("500");
  }
});

test("raw facade result carries the tagged type discriminator", async () => {
  const result = await riot4k.accountV1().getByRiotId("AMERICAS", "Hide on bush", "KR1");
  expect(result.type).toBe("success");
  expect(result.account?.gameName).toBe("Hide on bush");
});
