alter table "Dish"
add column price numeric(10,2);

update "Dish"
set price = 2000.00
where name = 'Salade fraîche';

update "Dish"
set price = 6000.00
where name = 'Poulet grillé';

update "Dish"
set price = null
where name = 'Riz au légumes';

update "Dish"
set price = null
where name = 'Gâteau au chocolat';

update "Dish"
set price = null
where name = 'Salade de fruit'

