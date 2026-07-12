import { Riot4KJs } from "riot4k-api";
import { AccountRepository } from "./accountRepository.js";

const apiKey = process.env.RIOT_API_KEY;
if (!apiKey) {
  throw new Error("Set the RIOT_API_KEY environment variable to your Riot API key");
}

const gameName = process.argv[2] ?? "Hide on bush";
const tagLine = process.argv[3] ?? "KR1";

const riot4k = new Riot4KJs(apiKey);
const repository = new AccountRepository(riot4k);
try {
  const lookup = await repository.lookup("AMERICAS", gameName, tagLine);
  switch (lookup.kind) {
    case "found":
      console.log(`${lookup.riotId} -> puuid=${lookup.puuid}`);
      break;
    case "missing":
      console.log(`No account with riot ID ${gameName}#${tagLine}`);
      break;
    case "error":
      console.log(`Lookup failed: ${lookup.message}`);
      break;
  }
} finally {
  riot4k.close();
}
