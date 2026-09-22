const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

async function processLogo() {
  const inputPath = 'C:\\Users\\utham\\.gemini\\antigravity-ide\\brain\\575a8148-ca82-4a93-a339-080a914ad9d0\\liquidstream_ai_logo_1788882347532.jpg';
  console.log('Loading image:', inputPath);

  const image = await Jimp.read(inputPath);
  const width = image.bitmap.width;
  const height = image.bitmap.height;
  console.log(`Original dimensions: ${width}x${height}`);

  // Create a visited array for flood-fill background removal from corners
  const visited = new Uint8Array(width * height);
  const queue = [];

  function isWhite(x, y) {
    const idx = (y * width + x) * 4;
    const r = image.bitmap.data[idx];
    const g = image.bitmap.data[idx + 1];
    const b = image.bitmap.data[idx + 2];
    // Background is near white (threshold > 230)
    return r > 235 && g > 235 && b > 235;
  }

  // Push borders to queue
  for (let x = 0; x < width; x++) {
    if (isWhite(x, 0)) { queue.push(x, 0); visited[0 * width + x] = 1; }
    if (isWhite(x, height - 1)) { queue.push(x, height - 1); visited[(height - 1) * width + x] = 1; }
  }
  for (let y = 0; y < height; y++) {
    if (isWhite(0, y)) { queue.push(0, y); visited[y * width + 0] = 1; }
    if (isWhite(width - 1, y)) { queue.push(width - 1, y); visited[y * width + (width - 1)] = 1; }
  }

  // BFS Flood-fill
  let head = 0;
  while (head < queue.length) {
    const cx = queue[head++];
    const cy = queue[head++];

    const neighbors = [
      [cx + 1, cy], [cx - 1, cy],
      [cx, cy + 1], [cx, cy - 1]
    ];

    for (const [nx, ny] of neighbors) {
      if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
        const nIdx = ny * width + nx;
        if (!visited[nIdx] && isWhite(nx, ny)) {
          visited[nIdx] = 1;
          queue.push(nx, ny);
        }
      }
    }
  }

  // Apply alpha transparency for visited background pixels with soft edge feathering
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const idx = (y * width + x) * 4;
      const vIdx = y * width + x;
      const r = image.bitmap.data[idx];
      const g = image.bitmap.data[idx + 1];
      const b = image.bitmap.data[idx + 2];

      if (visited[vIdx]) {
        image.bitmap.data[idx + 3] = 0; // completely transparent
      } else {
        // Check if pixel is near a transparent boundary for antialiasing
        const minVal = Math.min(r, g, b);
        if (minVal > 225) {
          // Check neighbors
          let hasTransNeighbor = false;
          if (x > 0 && visited[y * width + (x - 1)]) hasTransNeighbor = true;
          if (x < width - 1 && visited[y * width + (x + 1)]) hasTransNeighbor = true;
          if (y > 0 && visited[(y - 1) * width + x]) hasTransNeighbor = true;
          if (y < height - 1 && visited[(y + 1) * width + x]) hasTransNeighbor = true;

          if (hasTransNeighbor) {
            const alpha = Math.max(0, Math.min(255, Math.round((255 - minVal) * 8.5)));
            image.bitmap.data[idx + 3] = alpha;
          }
        }
      }
    }
  }

  console.log('Background removed successfully.');

  // Save 512x512 logo to res/drawable
  const resDrawableDir = path.resolve(__dirname, '../app/src/main/res/drawable');
  if (!fs.existsSync(resDrawableDir)) fs.mkdirSync(resDrawableDir, { recursive: true });
  
  const logo512 = image.clone().resize({ w: 512, h: 512 });
  await logo512.write(path.join(resDrawableDir, 'ic_app_logo.png'));
  console.log('Saved drawable/ic_app_logo.png (512x512)');

  // Also save launcher icons across all mipmap densities
  const densities = [
    { dir: 'mipmap-mdpi', size: 48 },
    { dir: 'mipmap-hdpi', size: 72 },
    { dir: 'mipmap-xhdpi', size: 96 },
    { dir: 'mipmap-xxhdpi', size: 144 },
    { dir: 'mipmap-xxxhdpi', size: 192 }
  ];

  const resDir = path.resolve(__dirname, '../app/src/main/res');

  for (const { dir, size } of densities) {
    const targetDir = path.join(resDir, dir);
    if (!fs.existsSync(targetDir)) fs.mkdirSync(targetDir, { recursive: true });

    const resized = image.clone().resize({ w: size, h: size });
    await resized.write(path.join(targetDir, 'ic_launcher.png'));
    await resized.write(path.join(targetDir, 'ic_launcher_round.png'));
    console.log(`Saved ${dir}/ic_launcher.png and ic_launcher_round.png (${size}x${size})`);
  }

  console.log('All launcher icons generated successfully!');
}

processLogo().catch(err => {
  console.error('Error processing logo:', err);
  process.exit(1);
});
