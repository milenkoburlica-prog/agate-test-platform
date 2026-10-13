import fs from "node:fs/promises";
import { parse } from "./model.js";
import { Runner } from "./runner.js";
const file = process.argv[2];
if (!file) {
  console.error("Usage: npm run cli -- project.yaml [variables.json]");
  process.exit(2);
}
const doc = parse(await fs.readFile(file, "utf8"));
const vars = process.argv[3]
  ? JSON.parse(await fs.readFile(process.argv[3], "utf8"))
  : {};
const runner = new Runner();
try {
  await runner.start(doc, { headed: false, vars });
  await runner.run();
  console.log(JSON.stringify(runner.state(), null, 2));
  process.exitCode = runner.status === "completed" ? 0 : 1;
} finally {
  await runner.close();
}
