import { Header } from "./Header";
import { Footer } from "./Footer";

export default function AppLayout({ children }) {
  return (
    <div className="flex flex-col min-h-screen bg-background">
      <Header />
      
      {/* flex-1 ensures the main content expands to push the footer to the bottom */}
      <main className="flex-1 p-8">
        {children}
      </main>
      
      <Footer />
    </div>
  );
}