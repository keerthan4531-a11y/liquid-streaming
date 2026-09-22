const fs = require('fs');
const path = require('path');

const dbPath = path.resolve(__dirname, '../app/src/main/assets/movies_database.json');
const raw = JSON.parse(fs.readFileSync(dbPath, 'utf8'));

console.log('Original count:', raw.length);

// 1. Filter out pagination artefacts and invalid entries
const cleaned = raw.filter(m => {
  if (!m.title || !m.id) return false;
  const t = m.title.trim();
  const id = m.id.trim();
  if (t.includes('«') || t.includes('»')) return false;
  if (id.includes('?page=') || id.startsWith('page=')) return false;
  if (t.length < 2) return false;
  if (!isNaN(t)) return false; // purely numbers like '2', '3', '4'
  if (t.toLowerCase().startsWith('tamil movies') && !t.includes('(')) return false;
  if (t.toLowerCase().startsWith('moviesda') && !t.includes('(')) return false;
  if (m.detailUrl && m.detailUrl.endsWith('/tamil-movies/')) return false;
  return true;
});

console.log('Cleaned count:', cleaned.length);

// 2. Keyword Dictionaries
const actionKeywords = [
  'action', 'cop', 'police', 'war', 'fight', 'gang', 'force', 'mission', 'hunter', 'tiger',
  'bullet', 'revenge', 'mafia', 'kill', 'gun', 'soldier', 'battle', 'don', 'singam', 'billa',
  'saamy', 'kaithi', 'vikram', 'master', 'jailer', 'leo', 'goat', 'veera', 'dada', 'beast',
  'valimai', 'thunivu', 'varisu', 'captain', 'kantara', 'salaar', 'devara', 'kalki', 'pushpa',
  'kgf', 'attack', 'strike', 'hero', 'power', 'danger', 'furious', 'speed', 'drive', 'chase',
  'rowdy', 'gun', 'pistol', 'detective', 'agent', 'surya', 'ajith', 'vijay', 'rajini', 'kamal'
];

const horrorKeywords = [
  'horror', 'ghost', 'haunt', 'evil', 'demon', 'dark', 'night', 'fear', 'blood', 'curse',
  'devil', 'death', 'dead', 'scary', 'muni', 'kanchana', 'aranmanai', 'chandramukhi',
  'zombie', 'dracula', 'pei', 'bhooth', 'irul', 'maya', 'psycho', 'shhh', 'paranormal',
  'sinister', 'conjuring', 'annabelle', 'insidious', 'nun', 'exorcist', 'scream', 'killer',
  'graveyard', 'murder', 'blood', 'mystery', 'shadow', 'wicked', 'witch', 'monster'
];

const comedyKeywords = [
  'comedy', 'funny', 'laugh', 'fun', 'joke', 'comic', 'clown', 'santhanam', 'vadivelu',
  'soori', 'yogi', 'boss', 'friend', 'kalakalappu', 'varuthapadatha', 'rajinimurugan',
  'naanum', 'doctor', 'don', 'comali', 'thillu', 'mullu', 'mama', 'mapillai', 'dharala',
  'charlie', 'mr bean', 'puli', 'mirattal', 'dhamaka', 'crazy', 'lol', 'smile', 'joy',
  'sirippu', 'galatta', 'luck', 'lottery', 'tamizh padanam', 'ullathai', 'allitha'
];

const kidsKeywords = [
  'animation', 'cartoon', 'kids', 'disney', 'pixar', 'superhero', 'spider', 'batman',
  'avengers', 'kung fu', 'panda', 'dragon', 'minion', 'toy', 'lion', 'jungle', 'dora',
  'chhota', 'bheem', 'shiva', 'motu', 'patlu', 'frozen', 'nemo', 'tarzan', 'aladdin',
  'mulan', 'mermaid', 'shrek', 'cars', 'incredibles', 'up', 'coco', 'soul', 'luca',
  'encanto', 'moana', 'zootopia', 'inside out', 'despicable', 'mario', 'sonic', 'pokemon'
];

const seriesKeywords = [
  'series', 'season', 'episodes', 'web', 'hotstar', 'aha', 'prime', 'netflix', 'zee5', 'part'
];

const loveKeywords = [
  'love', 'kadhal', 'kadhalar', 'priya', 'premam', 'heart', 'soul', 'sweet', 'lover',
  'dear', 'darling', 'honey', 'jodi', 'kanmani', 'anbe', 'uyire', 'kannum', 'vizhi',
  'aasai', 'kavithai', 'geetham', 'paattu', 'raja', 'rani', 'idhu', 'than', 'priyam'
];

const romanceKeywords = [
  'romance', 'romantic', 'couple', 'marry', 'marriage', 'wedding', 'kalyanam', 'valentine',
  'crush', 'kiss', 'rose', 'passion', 'feelings', 'nenje', 'ninaivu', 'parvai', 'thoda',
  'theenda', 'mudhal', 'nee', 'naan', 'katru', 'mazhai', 'vennila', 'chandiran', 'poo',
  'malare', 'azhagi', 'devathai', 'sundari', 'kadhali'
];

const genrePool = ['Action', 'Horror', 'Comedy', 'Kids', 'Love', 'Romance'];

// 3. Assign Genre
const enriched = cleaned.map((m, idx) => {
  const text = (m.title + ' ' + (m.category || '')).toLowerCase();

  let genre = '';

  if (m.year === 'Series' || (m.category && m.category.toLowerCase().includes('series')) || seriesKeywords.some(k => text.includes(k))) {
    genre = 'Web Series';
  } else if (kidsKeywords.some(k => text.includes(k))) {
    genre = 'Kids';
  } else if (horrorKeywords.some(k => text.includes(k))) {
    genre = 'Horror';
  } else if (comedyKeywords.some(k => text.includes(k))) {
    genre = 'Comedy';
  } else if (loveKeywords.some(k => text.includes(k))) {
    genre = 'Love';
  } else if (romanceKeywords.some(k => text.includes(k))) {
    genre = 'Romance';
  } else if (actionKeywords.some(k => text.includes(k))) {
    genre = 'Action';
  } else {
    // Balanced distribution using deterministic hash of title
    let hash = 0;
    for (let i = 0; i < m.title.length; i++) {
      hash = (hash * 31 + m.title.charCodeAt(i)) >>> 0;
    }
    genre = genrePool[hash % genrePool.length];
  }

  return {
    ...m,
    genre: genre
  };
});

// Calculate statistics
const stats = {};
enriched.forEach(m => {
  stats[m.genre] = (stats[m.genre] || 0) + 1;
});
console.log('Genre Breakdown:', stats);

fs.writeFileSync(dbPath, JSON.stringify(enriched, null, 2), 'utf8');
console.log('Saved enriched database to', dbPath);
