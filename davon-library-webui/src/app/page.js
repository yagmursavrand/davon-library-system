import Image from 'next/image';
import Link from 'next/link';
import { Inter } from 'next/font/google';

const inter = Inter({ subsets: ['latin'] });

export default function Home() {
  return (
    <main>
      {/* Header Section */}
      <header>
        <nav className="navbar" role="navigation" aria-label="Main navigation">
          <div className="logo">
            <Image src="/images/library-logo.jpg" alt="Davon Library Logo" width={50} height={50} />
            <h1>Davon Library</h1>
          </div>
          <ul className="nav-links">
            <li><a href="#home" aria-current="page">Home</a></li>
            <li><a href="#about">About</a></li>
            <li><a href="#services">Services</a></li>
            <li><a href="#events">Events</a></li>
            <li><a href="#contact">Contact</a></li>
          </ul>
        </nav>
      </header>

      {/* Hero Section */}
      <section id="home" className="hero">
        <div className="hero-content">
          <h2>Welcome to Davon Library</h2>
          <p>Your gateway to knowledge and discovery</p>
          <Link href="/register" className="cta-button">Register</Link>
        </div>
      </section>

      {/* Services Section */}
      <section id="services" className="services">
        <h2>Our Services</h2>
        <div className="search-container">
          <h3>Find Your Next Book</h3>
          <form className="search-bar" role="search">
            <label htmlFor="search-input" className="visually-hidden">Search books</label>
            <input type="search" id="search-input" placeholder="Search by title, author, or ISBN..." />
            <button type="submit" aria-label="Search">Search</button>
          </form>
        </div>
        <div className="services-grid">
          <article className="service-card">
            <i className="fas fa-book" aria-hidden="true"></i>
            <h3>Book Lending</h3>
            <p>Borrow books for up to 3 weeks</p>
          </article>
          <article className="service-card">
            <i className="fas fa-laptop" aria-hidden="true"></i>
            <h3>Digital Resources</h3>
            <p>Access e-books and online databases</p>
          </article>
          <article className="service-card">
            <i className="fas fa-users" aria-hidden="true"></i>
            <h3>Study Spaces</h3>
            <p>Quiet areas for reading and studying</p>
          </article>
        </div>
      </section>

      {/* Events Section */}
      <section id="events" className="events">
        <h2>Upcoming Events</h2>
        <div className="events-grid">
          <article className="event-card">
            <div className="event-date">
              <time dateTime="2024-05-15">
                <span className="day">15</span>
                <span className="month">May</span>
              </time>
            </div>
            <h3>Book Reading Club</h3>
            <p>Join us for our monthly book discussion</p>
            <a href="#register" className="event-button">Register</a>
          </article>
          <article className="event-card">
            <div className="event-date">
              <time dateTime="2024-05-20">
                <span className="day">20</span>
                <span className="month">May</span>
              </time>
            </div>
            <h3>Writing Workshop</h3>
            <p>Learn creative writing techniques</p>
            <a href="#register" className="event-button">Register</a>
          </article>
        </div>
      </section>

      {/* Footer */}
      <footer id="contact">
        <div className="footer-content">
          <section className="footer-section">
            <h3>Contact Us</h3>
            <address>
              <p><i className="fas fa-map-marker-alt" aria-hidden="true"></i> 123 Library Street, City</p>
              <p><i className="fas fa-phone" aria-hidden="true"></i> <a href="tel:+12345678900">+1 234 567 890</a></p>
              <p><i className="fas fa-envelope" aria-hidden="true"></i> <a href="mailto:info@davonlibrary.com">info@davonlibrary.com</a></p>
            </address>
          </section>
          <section className="footer-section">
            <h3>Opening Hours</h3>
            <p>Monday - Friday: 9:00 AM - 8:00 PM</p>
            <p>Saturday: 10:00 AM - 6:00 PM</p>
            <p>Sunday: Closed</p>
          </section>
          <section className="footer-section">
            <h3>Follow Us</h3>
            <div className="social-links">
              <a href="#" aria-label="Visit our Facebook page"><i className="fab fa-facebook" aria-hidden="true"></i></a>
              <a href="#" aria-label="Follow us on Twitter"><i className="fab fa-twitter" aria-hidden="true"></i></a>
              <a href="#" aria-label="Follow us on Instagram"><i className="fab fa-instagram" aria-hidden="true"></i></a>
            </div>
          </section>
        </div>
        <div className="footer-bottom">
          <p>&copy; 2024 Davon Library. All rights reserved.</p>
        </div>
      </footer>
    </main>
  );
} 