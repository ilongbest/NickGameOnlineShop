const config = window.GSHOP_ACCOUNT_LIST || {};

let currentPage = 0;
let totalPages = 1;
const pageSize = 9;

const tagLabels = {
    VIP: 'VIP',
    CHEAP: 'Gia re',
    RANK_HIGH: 'Rank cao',
    SKIN_MANY: 'Nhieu skin'
};

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('filterButton').addEventListener('click', applyFilters);
    document.getElementById('prevPageBtn').addEventListener('click', () => changePage(-1));
    document.getElementById('nextPageBtn').addEventListener('click', () => changePage(1));
    document.getElementById('searchId').addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            applyFilters();
        }
    });
    loadAccounts();
});

async function loadAccounts() {
    const container = document.getElementById('nickContainer');
    container.innerHTML = "<p class='empty-message'>Dang tai danh sach nick...</p>";

    try {
        const response = await fetch(buildAccountUrl());
        if (!response.ok) {
            throw new Error(await response.text());
        }

        const pageData = await response.json();
        totalPages = pageData.totalPages || 1;
        renderAccounts(pageData.content || []);
        updatePagination();
    } catch (error) {
        console.error('Loi tai du lieu nick:', error);
        container.innerHTML = "<p class='empty-message'>Khong the ket noi den may chu.</p>";
    }
}

function buildAccountUrl() {
    const params = new URLSearchParams({
        page: currentPage,
        size: pageSize,
        available: document.getElementById('availableFilter').value
    });

    const searchId = document.getElementById('searchId').value.trim();
    const sort = document.getElementById('sortFilter').value;
    const tag = document.getElementById('tagFilter').value;

    if (searchId) {
        params.set('searchId', searchId);
    }
    if (sort) {
        params.set('sort', sort);
    }
    if (tag && tag !== 'all') {
        params.set('tag', tag);
    }

    return `/api/v1/accounts/category/${config.categoryId}?${params.toString()}`;
}

function renderAccounts(accounts) {
    const container = document.getElementById('nickContainer');

    if (!accounts.length) {
        container.innerHTML = "<p class='empty-message'>Khong tim thay nick phu hop.</p>";
        return;
    }

    container.innerHTML = accounts.map((acc) => {
        const imgUrl = acc.images && acc.images.length > 0
            ? acc.images[0].url
            : 'https://via.placeholder.com/400x220?text=No+Image';
        const description = acc.description || 'Tai khoan game dang ban tren he thong G-SHOP.';
        const statusText = acc.status === 0 ? 'Con hang' : 'Da ban / tam an';
        const statusClass = acc.status === 0 ? 'status-available' : 'status-hidden';

        return `
            <article class="nick-card">
                <div class="nick-img">
                    ${renderTag(acc.tag)}
                    <span class="nick-id">#${escapeHtml(acc.id)}</span>
                    <img src="${escapeHtml(imgUrl)}" alt="${escapeHtml(config.gameName || 'Game account')}">
                </div>
                <div class="nick-info">
                    <div class="nick-title">${escapeHtml(acc.accUsername || 'Tai khoan game')}</div>
                    <div class="nick-desc">${escapeHtml(description)}</div>
                    <div class="status-line ${statusClass}">${statusText}</div>
                    <div class="nick-footer">
                        <div class="price">${new Intl.NumberFormat('vi-VN').format(acc.price || 0)}đ</div>
                        <div class="btn-actions">
                            <button class="btn-buy" onclick="location.href='/nick-detail/${acc.id}'">Chi tiet</button>
                            <button class="btn-cart" onclick="addToCart(${acc.id})" title="Them vao gio hang">
                                <i class="fas fa-cart-plus"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </article>
        `;
    }).join('');
}

function renderTag(tag) {
    if (!tag || !tagLabels[tag]) {
        return '';
    }
    return `<span class="tag tag-${tag.toLowerCase()}">${tagLabels[tag]}</span>`;
}

function applyFilters() {
    currentPage = 0;
    loadAccounts();
}

function updatePagination() {
    document.getElementById('pageInfo').innerText = `Trang ${currentPage + 1}/${totalPages}`;
    document.getElementById('prevPageBtn').disabled = currentPage <= 0;
    document.getElementById('nextPageBtn').disabled = currentPage >= totalPages - 1;
}

function changePage(step) {
    const nextPage = currentPage + step;
    if (nextPage < 0 || nextPage >= totalPages) {
        return;
    }
    currentPage = nextPage;
    loadAccounts();
}

function addToCart(id) {
    const items = JSON.parse(localStorage.getItem('gshopCartItems') || '[]');
    if (!items.some((item) => Number(item.accountId) === Number(id))) {
        items.push({ accountId: id, game: config.cartGame || config.gameName || 'GAME', addedAt: Date.now() });
        localStorage.setItem('gshopCartItems', JSON.stringify(items));
    }
    alert('Da them vao gio hang!');
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
