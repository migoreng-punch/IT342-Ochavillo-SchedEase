import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import { EstablishmentCard } from './EstablishmentCard';

describe('EstablishmentCard Component', () => {
  it('generates the correct Booking URL using establishment.id', () => {
    // 1. Arrange: Create the object structure your component expects
    const mockEstablishment = {
      id: 10,
      name: 'Test Establishment',
      description: 'A sample description for testing.'
    };

    render(
      <BrowserRouter>
        {/* 2. Pass the object into the "establishment" prop */}
        <EstablishmentCard establishment={mockEstablishment} />
      </BrowserRouter>
    );

    // 3. Act
    const linkElement = screen.getByRole('link', { name: /view availability/i });

    // 4. Assert: It should now find '10' instead of 'undefined'
    expect(linkElement.getAttribute('href')).toBe('/establishment/10');
  });
});