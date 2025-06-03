class CartManager {
    constructor() {
        this.token = document.querySelector('meta[name="_csrf"]')?.content;
        this.header = document.querySelector('meta[name="_csrf_header"]')?.content;
        this.init();
    }

    init() {
        this.updateCartBadge();
        this.setupEventListeners();
    }


    setupEventListeners() {
        document.addEventListener('click', (e) => {
            if (e.target.closest('.add-to-cart-btn')) {
                e.preventDefault();
                const button = e.target.closest('.add-to-cart-btn');
                const productId = button.dataset.productId;

                const quantityInputId = button.dataset.quantityInput;
                let quantity = button.dataset.quantity || 1;

                if (quantityInputId) {
                    const quantityInput = document.getElementById(quantityInputId);
                    if (quantityInput) {
                        quantity = quantityInput.value;
                    }
                }

                this.addToCart(productId, quantity, button);
            }
        });
    }

    async addToCart(productId, quantity = 1, button = null) {
        if (button) {
            button.disabled = true;
            const originalText = button.innerHTML;
            button.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>Dodajem...';
        }

        try {
            const response = await fetch('/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [this.header]: this.token
                },
                body: `productId=${productId}&quantity=${quantity}`
            });

            const data = await response.json();

            if (data.success) {
                this.showNotification('success', data.message);
                this.updateCartBadge(data.cartItemCount);

                if (button) {
                    button.innerHTML = '<i class="bi bi-check me-1"></i>Dodano!';
                    setTimeout(() => {
                        button.innerHTML = '<i class="bi bi-cart-plus me-1"></i>Dodaj';
                        button.disabled = false;
                    }, 2000);
                }
            } else {
                this.showNotification('error', data.message);
                if (button) {
                    button.disabled = false;
                    button.innerHTML = originalText;
                }
            }
        } catch (error) {
            this.showNotification('error', 'Greška pri dodavanju u košaricu');
            if (button) {
                button.disabled = false;
                button.innerHTML = originalText;
            }
        }
    }

    async updateCartBadge(count = null) {
        if (count !== null) {
            this.setBadgeCount(count);
        } else {
            try {
                const response = await fetch('/cart/count');
                const count = await response.json();
                this.setBadgeCount(count);
            } catch (error) {
            }
        }
    }

    setBadgeCount(count) {
        const badges = document.querySelectorAll('.cart-badge');
        badges.forEach(badge => {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline' : 'none';
        });
    }

    showNotification(type, message) {
        const existingAlert = document.querySelector('.alert-floating');
        if (existingAlert) {
            existingAlert.remove();
        }

        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show alert-floating`;
        alertDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: slideInRight 0.3s ease-out;
        `;

        alertDiv.innerHTML = `
            <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-triangle'} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        document.body.appendChild(alertDiv);

        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.classList.remove('show');
                setTimeout(() => alertDiv.remove(), 150);
            }
        }, 5000);
    }
}

const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    .alert-floating {
        animation: slideInRight 0.3s ease-out;
    }
`;
document.head.appendChild(style);

document.addEventListener('DOMContentLoaded', () => {
    window.cartManager = new CartManager();
});