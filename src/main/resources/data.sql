-- 초기 데이터 설정
USE cafe_db;

-- 1. 초기 카테고리 등록
INSERT INTO categories (name)
SELECT '싱글오리진'
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE name = '싱글오리진'
);

-- 2. 메뉴 초기 데이터 등록
INSERT INTO menus (category_id, name, price, description, available)
SELECT
    c.id,
    m.name,
    m.price,
    m.description,
    TRUE
FROM categories c
         CROSS JOIN (
    SELECT
        'Columbia Nariñó' AS name,
        5000 AS price,
        '콜롬비아 나리뇨 원두를 담은 커피빈 패키지입니다.' AS description
    UNION ALL
    SELECT
        'Brazil Serra Do Caparaó',
        7000,
        '브라질 세하 두 카파라오 원두를 담은 커피빈 패키지입니다.'
    UNION ALL
    SELECT
        'Columbia Quindío (White Wine Extended Fermentation)',
        9000,
        '콜롬비아 킨디오 원두를 담은 커피빈 패키지입니다.'
    UNION ALL
    SELECT
        'Ethiopia Sidamo',
        11000,
        '에티오피아 시다모 원두를 담은 커피빈 패키지입니다.'
) m
WHERE c.name = '싱글오리진'
  AND NOT EXISTS (
    SELECT 1
    FROM menus existing
    WHERE existing.name = m.name
);