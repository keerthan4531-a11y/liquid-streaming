const https = require('https');
const fs = require('fs');
const path = require('path');

const BASE = 'https://moviezda.com';

function fetchUrl(url, timeoutMs = 12000) {
  return new Promise((resolve, reject) => {
    const req = https.get(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
        'Accept-Language': 'en-US,en;q=0.9',
        'Referer': 'https://moviezda.com/'
      },
      timeout: timeoutMs
    }, (res) => {
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        let redirectUrl = res.headers.location;
        if (redirectUrl.startsWith('/')) redirectUrl = BASE + redirectUrl;
        return fetchUrl(redirectUrl, timeoutMs).then(resolve).catch(reject);
      }
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => resolve(data));
    });
    req.on('error', reject);
    req.on('timeout', () => {
      req.destroy();
      reject(new Error(`Timeout fetching ${url}`));
    });
  });
}

function derivePosterUrl(slug, title, year) {
  const cleanSlug = slug
    .replace(/-tamil-web-series$/, '')
    .replace(/-tamil-movie$/, '')
    .replace(/-moviesda$/, '')
    .replace(/-web-series$/, '')
    .replace(/-movie$/, '')
    .replace(/-series$/, '')
    .replace(/-season-\d+$/, '')
    .replace(/-hd$/, '');

  const hasYear = /\b(20\d\d|19\d\d)\b/.test(cleanSlug);
  if (hasYear) {
    return `${BASE}/uploads/posters/${cleanSlug}.jpg`;
  }
  const yMatch = title.match(/\b(20\d\d|19\d\d)\b/);
  if (yMatch) {
    return `${BASE}/uploads/posters/${cleanSlug}-${yMatch[1]}.jpg`;
  }
  if (year && /^\d{4}$/.test(year)) {
    return `${BASE}/uploads/posters/${cleanSlug}-${year}.jpg`;
  }
  return `${BASE}/uploads/posters/${cleanSlug}.jpg`;
}

// Categories configuration with known max pages and pagination styles
const CATEGORIES = [
  { slug: 'moviesda-tamil-movies-2026', defaultYear: '2026', category: 'Tamil 2026 Movies', maxPages: 18, isGetPage: false },
  { slug: 'tamil-2026-movies', defaultYear: '2026', category: 'Tamil 2026 Movies', maxPages: 18, isGetPage: false },
  { slug: 'tamil-2025-movies', defaultYear: '2025', category: 'Tamil 2025 Movies', maxPages: 25, isGetPage: false },
  { slug: 'tamil-2024-movies', defaultYear: '2024', category: 'Tamil 2024 Movies', maxPages: 25, isGetPage: false },
  { slug: 'tamil-2023-movies', defaultYear: '2023', category: 'Tamil 2023 Movies', maxPages: 19, isGetPage: false },
  { slug: 'tamil-2022-movies', defaultYear: '2022', category: 'Tamil 2022 Movies', maxPages: 20, isGetPage: false },
  { slug: 'tamil-2021-movies', defaultYear: '2021', category: 'Tamil 2021 Movies', maxPages: 19, isGetPage: false },
  { slug: 'tamil-2020-movies', defaultYear: '2020', category: 'Tamil 2020 Movies', maxPages: 11, isGetPage: false },
  { slug: 'tamil-2019-movies', defaultYear: '2019', category: 'Tamil 2019 Movies', maxPages: 10, isGetPage: false },
  { slug: 'tamil-2018-movies', defaultYear: '2018', category: 'Tamil 2018 Movies', maxPages: 9, isGetPage: false },
  { slug: 'tamil-2017-movies', defaultYear: '2017', category: 'Tamil 2017 Movies', maxPages: 10, isGetPage: false },
  { slug: 'tamil-2016-movies', defaultYear: '2016', category: 'Tamil 2016 Movies', maxPages: 8, isGetPage: false },
  { slug: 'tamil-2015-movies', defaultYear: '2015', category: 'Tamil 2015 Movies', maxPages: 5, isGetPage: false },
  { slug: 'tamil-2012-movies', defaultYear: '2012', category: 'Tamil 2012 Movies', maxPages: 1, isGetPage: false },
  { slug: 'tamil-web-series-download', defaultYear: 'Series', category: 'Web Series', maxPages: 7, isGetPage: true },
  { slug: 'tamil-dubbed-movies', defaultYear: 'Dubbed', category: 'Tamil Dubbed', maxPages: 5, isGetPage: false },
  { slug: 'tamil-movies-collection', defaultYear: 'Collections', category: 'Collections', maxPages: 2, isGetPage: false },
  { slug: 'moviesda-tamil-collections', defaultYear: 'Single Parts', category: 'Single Parts', maxPages: 1, isGetPage: false },
  { slug: 'tamil-hd-movies', defaultYear: 'HD Mobile', category: 'HD Mobile', maxPages: 2, isGetPage: false }
];

async function scrapePage(cat, pageNum) {
  let url;
  if (pageNum === 1) {
    url = `${BASE}/${cat.slug}/`;
  } else if (cat.isGetPage) {
    url = `${BASE}/${cat.slug}/?get-page=${pageNum}`;
  } else {
    url = `${BASE}/${cat.slug}/?page=${pageNum}`;
  }

  try {
    const html = await fetchUrl(url);
    const items = [];

    // 1. Grid layout with li.movie-list-item (2026 releases)
    const gridRegex = /<li[^>]*class=["']movie-list-item["'][^>]*>([\s\S]*?)<\/li>/gi;
    let gm;
    while ((gm = gridRegex.exec(html)) !== null) {
      const block = gm[1];
      const hrefMatch = block.match(/href=["']([^"']+)["']/i);
      const titleMatch = block.match(/<h3[^>]*class=["']movie-title["'][^>]*>(.*?)<\/h3>/i);
      const ratingMatch = block.match(/<span[^>]*class=["']movie-rating["'][^>]*>(.*?)<\/span>/i);
      const badgeMatch = block.match(/<span[^>]*class=["']tiny-new-badge["'][^>]*>(.*?)<\/span>/i);
      const posterMatch = block.match(/<img[^>]*class=["']movie-poster["'][^>]*src=["']([^"']+)["']/i);

      if (hrefMatch && titleMatch) {
        const href = hrefMatch[1];
        const id = href.replace(/\/$/, '').split('/').pop();
        const title = titleMatch[1].replace(/<[^>]+>/g, '').trim();
        const rating = ratingMatch ? ratingMatch[1].trim() : '8.8';
        const badge = badgeMatch ? badgeMatch[1].trim() : (cat.defaultYear === '2026' ? 'NEW' : 'HD');
        let poster = posterMatch ? posterMatch[1] : '';
        if (poster.startsWith('/')) poster = BASE + poster;
        if (!poster || poster.includes('folder') || poster.includes('assets')) {
          poster = derivePosterUrl(id, title, cat.defaultYear);
        }

        const yMatch = title.match(/\b(20\d\d|19\d\d)\b/);
        const itemYear = yMatch ? yMatch[1] : cat.defaultYear;
        const fullUrl = href.startsWith('http') ? href : `${BASE}${href.startsWith('/') ? '' : '/'}${href}`;

        items.push({
          id,
          title,
          posterUrl: poster,
          detailUrl: fullUrl,
          rating,
          badge,
          year: itemYear,
          category: cat.category
        });
      }
    }

    // 2. Directory layout (standard div.f a or plain a tags)
    if (items.length === 0) {
      const linkRegex = /<a[^>]+href=["']([^"']+)["'][^>]*>(.*?)<\/a>/gi;
      let lm;
      while ((lm = linkRegex.exec(html)) !== null) {
        const href = lm[1];
        const rawText = lm[2].replace(/<[^>]+>/g, '').trim();
        if (!rawText || href.startsWith('#') || href.startsWith('javascript:')) continue;
        if (href.includes('/tamil-movies/') || href.includes('/page/') || href.includes('/category/') || href.includes('disclaimer') || href.includes('contact')) continue;

        if (href.includes('-movie') || href.includes('-series') || href.includes('season') || href.includes('-web-series') || href.includes('tamil-')) {
          const id = href.replace(/\/$/, '').split('/').pop();
          if (!id || id.length < 3) continue;

          const yMatch = rawText.match(/\b(20\d\d|19\d\d)\b/);
          const itemYear = yMatch ? yMatch[1] : cat.defaultYear;
          const cleanTitle = rawText.replace(/-\s*Moviesda.*$/i, '').trim();
          const fullUrl = href.startsWith('http') ? href : `${BASE}${href.startsWith('/') ? '' : '/'}${href}`;

          items.push({
            id,
            title: cleanTitle,
            posterUrl: derivePosterUrl(id, cleanTitle, itemYear),
            detailUrl: fullUrl,
            rating: (7.8 + (Math.random() * 1.8)).toFixed(1),
            badge: itemYear === '2026' ? 'NEW' : (cat.defaultYear === 'Series' ? 'SERIES' : 'HD'),
            year: itemYear,
            category: cat.category
          });
        }
      }
    }

    return items;
  } catch (err) {
    console.error(`  [!] Error scraping ${cat.slug} page ${pageNum}: ${err.message}`);
    return [];
  }
}

async function scrapeAll() {
  console.log('=== STARTING MOVIESDA MASTER REVERSE-ENGINEERED SCRAPER ===');
  const masterMovies = new Map();

  // Load existing database if available to preserve any unique entries
  const targetPath = path.join(__dirname, '..', 'app', 'src', 'main', 'assets', 'movies_database.json');
  if (fs.existsSync(targetPath)) {
    try {
      const existing = JSON.parse(fs.readFileSync(targetPath, 'utf8'));
      console.log(`Loaded ${existing.length} existing movies from database to merge.`);
      existing.forEach(m => masterMovies.set(m.id, m));
    } catch (e) {
      console.warn('Could not read existing database, starting fresh.');
    }
  }

  let totalNew = 0;

  for (const cat of CATEGORIES) {
    console.log(`\nCrawling Category: ${cat.category} (${cat.slug}) [${cat.maxPages} pages]...`);
    let catNew = 0;

    for (let p = 1; p <= cat.maxPages; p++) {
      const items = await scrapePage(cat, p);
      for (const item of items) {
        if (!masterMovies.has(item.id)) {
          masterMovies.set(item.id, item);
          catNew++;
          totalNew++;
        } else {
          // If existing had generic poster or missing category, enrich it
          const current = masterMovies.get(item.id);
          if (cat.defaultYear === 'Series' && current.year !== 'Series') {
            current.year = 'Series';
            current.category = 'Web Series';
            current.badge = 'SERIES';
          }
          if (cat.defaultYear === 'Dubbed' && current.year !== 'Dubbed') {
            current.category = 'Tamil Dubbed';
          }
        }
      }
      process.stdout.write(`  Page ${p}/${cat.maxPages} (${items.length} items) `);
      // Small pause to be polite
      await new Promise(r => setTimeout(r, 80));
    }
    console.log(`\n  Category ${cat.category} Done -> ${catNew} new titles added!`);
  }

  const allMovies = Array.from(masterMovies.values());

  // Sort: 2026 first, then 2025, 2024, etc., then Series, Dubbed
  allMovies.sort((a, b) => {
    const ya = parseInt(a.year) || (a.year === '2026' ? 2026 : (a.year === 'Series' ? 2025.5 : 2000));
    const yb = parseInt(b.year) || (b.year === '2026' ? 2026 : (b.year === 'Series' ? 2025.5 : 2000));
    if (yb !== ya) return yb - ya;
    return a.title.localeCompare(b.title);
  });

  // Calculate stats
  const stats = {};
  allMovies.forEach(m => {
    stats[m.year] = (stats[m.year] || 0) + 1;
  });

  console.log('\n========================================');
  console.log(`TOTAL MOVIES & SERIES IN MASTER DB: ${allMovies.length}`);
  console.log(`NEW TITLES ADDED IN THIS CRAWL: ${totalNew}`);
  console.log('Year & Category Breakdown:');
  console.log(JSON.stringify(stats, null, 2));
  console.log('========================================');

  // Save to assets
  fs.writeFileSync(targetPath, JSON.stringify(allMovies, null, 2), 'utf8');
  console.log(`Successfully saved master database to: ${targetPath}`);
}

scrapeAll().catch(err => {
  console.error('Fatal scrape error:', err);
  process.exit(1);
});
