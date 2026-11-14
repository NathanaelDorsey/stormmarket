#!/usr/bin/env node

/**
 * Generate Postman Collection v2.1 JSON from v2 directory format
 * Run with: node scripts/generate-collection.js
 */

const fs = require('fs');
const path = require('path');
const yaml = require('js-yaml');

const COLLECTION_DIR = path.join(__dirname, '../postman/collections/Storm Market API');
const OUTPUT_FILE = path.join(__dirname, '../postman/collections/Storm_Market_API.json');

function parseYamlFile(filePath) {
  const content = fs.readFileSync(filePath, 'utf8');
  const lines = content.split('\n');
  if (lines[0].startsWith('$kind:')) {
    lines.shift();
  }
  return yaml.load(lines.join('\n'));
}

function walkDir(dir) {
  const items = [];
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (entry.name !== '.resources') {
        const folderItems = walkDir(fullPath);
        if (folderItems.length > 0) {
          items.push({
            name: entry.name,
            item: folderItems
          });
        }
      }
    } else if (entry.name.endsWith('.request.yaml')) {
      const request = parseYamlFile(fullPath);
      items.push(convertRequest(request));
    }
  }
  return items;
}

function convertRequest(req) {
  const item = {
    name: req.description || req.url,
    request: {
      method: req.method,
      header: [],
      url: buildUrlObject(req.url)
    }
  };

  if (req.headers) {
    item.request.header = req.headers.map(h => ({
      key: h.key,
      value: h.value,
      type: 'text'
    }));
  }

  if (req.body) {
    if (req.body.type === 'json') {
      item.request.body = {
        mode: 'raw',
        raw: req.body.content,
        options: { raw: { language: 'json' } }
      };
    }
  }

  if (req.auth) {
    item.request.auth = {
      type: req.auth.type,
      bearer: req.auth.credentials ? [{
        key: 'token',
        value: req.auth.credentials[0]?.value || '{{authToken}}',
        type: 'string'
      }] : []
    };
  }

  if (req.scripts) {
    item.event = req.scripts.map(s => ({
      listen: s.type === 'beforeRequest' ? 'prerequest' : 'test',
      script: { type: 'text/javascript', exec: s.code.split('\n') }
    }));
  }

  return item;
}

function buildUrlObject(url) {
  // Use raw URL only - let Postman resolve {{baseUrl}} variable
  return { raw: url };
}

// Generate collection
const collection = {
  info: {
    name: "Storm Market API",
    schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    description: "Storm Market API test collection"
  },
  variable: [
    { key: "baseUrl", value: "", type: "string" },
    { key: "authToken", value: "", type: "string" }
  ],
  auth: {
    type: "bearer",
    bearer: [{ key: "token", value: "{{authToken}}", type: "string" }]
  },
  item: walkDir(COLLECTION_DIR)
};

fs.writeFileSync(OUTPUT_FILE, JSON.stringify(collection, null, 2));
console.log(`Collection written to ${OUTPUT_FILE}`);
console.log(`Total folders: ${collection.item.length}`);
let totalRequests = 0;
function countRequests(items) {
  for (const item of items) {
    if (item.request) totalRequests++;
    if (item.item) countRequests(item.item);
  }
}
countRequests(collection.item);
console.log(`Total requests: ${totalRequests}`);