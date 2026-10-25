// حساباتي — بيخلّي التطبيق يفتح من غير نت.
// الصفحة نفسها: بيجيب أحدث نسخة لو فيه نت (عشان أي تحديث ترفعه يوصلك)، ولو مفيش بيفتح المحفوظة.
// بياناتك مش هنا — بياناتك في تخزين المتصفح على جهازك، والملف ده مالوش دعوة بيها.
const CACHE = 'hesabaty-v3';
const FILES = ['./', 'index.html', 'manifest.webmanifest', 'icon-192.png', 'icon-512.png', 'icon-180.png', 'favicon-96.png'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET' || new URL(req.url).origin !== location.origin) return;
  if (req.mode === 'navigate') {
    // الصفحة: النت الأول، وبعدين المحفوظ
    e.respondWith(
      fetch(req)
        .then(res => { const copy = res.clone(); caches.open(CACHE).then(c => c.put('index.html', copy)); return res; })
        .catch(() => caches.match('index.html').then(r => r || caches.match('./')))
    );
    return;
  }
  // الأيقونات والملفات التانية: المحفوظ الأول
  e.respondWith(caches.match(req).then(r => r || fetch(req).then(res => {
    const copy = res.clone(); caches.open(CACHE).then(c => c.put(req, copy)); return res;
  })));
});
