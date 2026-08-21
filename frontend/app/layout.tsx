import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import './globals.css';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  metadataBase: new URL('https://inventarizaciya-itam.terontyn.chatgpt.site'),
  title: 'Inventarizaciya — IT Asset Management',
  description: 'Операционное пространство для управления IT-активами компании.',
  openGraph: {
    title: 'Inventarizaciya — IT Asset Management',
    description: 'Управление IT-активами без хаоса.',
    url: 'https://inventarizaciya-itam.terontyn.chatgpt.site',
    siteName: 'Inventarizaciya',
    locale: 'ru_RU',
    type: 'website',
    images: [{
      url: 'https://inventarizaciya-itam.terontyn.chatgpt.site/og.png',
      width: 1200,
      height: 630,
      alt: 'Inventarizaciya — управление IT-активами без хаоса',
    }],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Inventarizaciya — IT Asset Management',
    description: 'Управление IT-активами без хаоса.',
    images: ['https://inventarizaciya-itam.terontyn.chatgpt.site/og.png'],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ru">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        {children}
      </body>
    </html>
  );
}
