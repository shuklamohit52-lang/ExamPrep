import express from 'express';
import cors from 'cors';
import multer from 'multer';
import path from 'path';

const app = express();
const upload = multer({ dest: 'uploads/' });
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json());
app.use('/admin', express.static(path.resolve('backend/admin')));

const tests = [
  { id: 't1', title: 'SSC CGL Mock Test Series 2026', total_tests: 969, free_tests: 9, languages: ['English', 'Hindi'] },
  { id: 't2', title: 'Railway Current Affairs Pack', total_tests: 449, free_tests: 21, languages: ['English', 'Hindi'] }
];

const resources = [
  { id: 'r1', title: 'Daily Current Affairs PDF', category: 'pdf', download_url: 'https://example.com/ca.pdf', is_free: true },
  { id: 'r2', title: 'Quant eBook', category: 'ebook', download_url: 'https://example.com/quant.epub', is_free: false }
];

const currentAffairs = [
  { id: 'ca1', headline: 'RBI releases new policy update', date: '2026-01-04', pdf_url: 'https://example.com/ca-0104.pdf' },
  { id: 'ca2', headline: 'ISRO mission success update', date: '2026-01-05', pdf_url: 'https://example.com/ca-0105.pdf' }
];

app.post('/api/auth/login', (req, res) => {
  const { email } = req.body;
  res.json({ token: 'demo-jwt-token', user: { id: 'u1', email, plan: 'free' } });
});

app.get('/api/tests', (_req, res) => res.json(tests));
app.get('/api/resources', (_req, res) => res.json(resources));
app.get('/api/current-affairs', (_req, res) => res.json(currentAffairs));

app.post('/api/subscriptions/checkout', (req, res) => {
  const { planId, userId } = req.body;
  res.json({ status: 'created', paymentSessionId: `sess_${planId}_${userId}` });
});

app.post('/api/subscriptions/webhook', (req, res) => {
  console.log('payment webhook', req.body);
  res.status(200).json({ ok: true });
});

app.post('/api/admin/tests', upload.single('thumbnail'), (req, res) => {
  const record = { id: `t${tests.length + 1}`, ...req.body };
  tests.push(record);
  res.status(201).json(record);
});

app.post('/api/admin/resources', upload.single('file'), (req, res) => {
  const record = { id: `r${resources.length + 1}`, ...req.body, download_url: req.file?.path ?? '' };
  resources.push(record);
  res.status(201).json(record);
});

app.listen(PORT, () => {
  console.log(`ExamPrep backend listening on http://localhost:${PORT}`);
});
