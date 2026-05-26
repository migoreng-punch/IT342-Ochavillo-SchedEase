import React from 'react';
import { Header } from '../layouts/Header';
import HeroSection from '../components/HeroSection';
import FeaturesSection from '../components/FeaturesSection';
import HowItWorksSection from '../components/HowItWorksSection';
import CtaSection from '../components/CtaSection';
import { Footer } from '../layouts/Footer';

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-white font-sans">
      <Header />
      <main>
        <HeroSection />
        <FeaturesSection />
        <HowItWorksSection />
        <CtaSection />
        <Footer />
      </main>
    </div>
  );
}