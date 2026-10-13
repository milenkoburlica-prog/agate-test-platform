import fs from "node:fs/promises";
import path from "node:path";
export class RecorderJournal {
  constructor(root) {
    this.root = root;
    this.rows = [];
    this.count = 0;
  }
  async start() {
    this.id = new Date().toISOString().replace(/[:.]/g, "-");
    this.file = path.join(this.root, "recordings", this.id, "recorder.jsonl");
    await fs.mkdir(path.dirname(this.file), { recursive: true });
    this.rows = [];
    this.count = 0;
    await fs.writeFile(this.file, "");
    await this.append({
      kind: "session",
      outcome: "started",
      version: "0.1.4",
    });
  }
  async append(data) {
    if (!this.file) return;
    const row = {
      serverSequence: ++this.count,
      receivedAt: new Date().toISOString(),
      ...data,
    };
    this.rows.push(row);
    if (this.rows.length > 300) this.rows.shift();
    await fs.appendFile(this.file, JSON.stringify(row) + "\n");
  }
  state() {
    return { id: this.id, count: this.count, rows: this.rows };
  }
}
