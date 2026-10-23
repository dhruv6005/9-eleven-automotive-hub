import { readdir, readFile } from 'node:fs/promises';
import { join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('../src/', import.meta.url));
const allowed = new Set(['.ts', '.html', '.css']);
const issues = [];

async function visit(directory) {
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    const fullPath = join(directory, entry.name);
    if (entry.isDirectory()) {
      await visit(fullPath);
    } else if (allowed.has(fullPath.slice(fullPath.lastIndexOf('.')))) {
      const content = await readFile(fullPath, 'utf8');
      const lines = content.split(/\r?\n/);
      lines.forEach((line, index) => {
        if (/[\t ]+$/.test(line)) {
          issues.push(`${relative(root, fullPath)}:${index + 1}: trailing whitespace`);
        }
      });
    }
  }
}

await visit(root);
if (issues.length) {
  console.error(issues.join('\n'));
  process.exitCode = 1;
} else {
  console.log('Source hygiene check passed. (Angular ESLint rules can be added with the feature modules.)');
}
