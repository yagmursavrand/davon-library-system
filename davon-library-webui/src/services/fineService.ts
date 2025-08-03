// src/services/fineService.ts

export interface Fine {
  id: number;
  bookTitle: string;
  amount: string;
  issuedDate: string;
  reason: string;
}

class FineService {
  private API_BASE_URL = 'http://localhost:8082/api';

  async getUnpaidFines(memberId: string): Promise<Fine[]> {
    const response = await fetch(`${this.API_BASE_URL}/fines/member/${memberId}`, {
      headers: {
        'Authorization': `Bearer ${memberId}`,
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Failed to fetch fines');
    }

    return response.json();
  }

  async payAllFines(memberId: string): Promise<void> {
    const response = await fetch(`${this.API_BASE_URL}/fines/member/${memberId}/pay`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${memberId}`,
      },
    });

    if (!response.ok) {
        // We still want to throw an error for actual server errors like 500, 404, etc.
        const errorText = await response.text().catch(() => 'Server returned an error');
        throw new Error(errorText || 'Failed to pay fines');
    }
    
    // A 204 No Content response is considered 'ok' but has no body.
    // By not trying to parse JSON here, we handle it gracefully.
  }
}

const fineService = new FineService();
export default fineService;
