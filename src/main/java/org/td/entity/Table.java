package org.td.entity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Table {
    private Integer id;
    private int number;
    private List<Order> orders;


    public Table(Integer id, int number, List<Order> orders) {
        this.id = id;
        this.number = number;
        this.orders = orders;
    }

    public Table() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return number == table.number && Objects.equals(id, table.id) && Objects.equals(orders, table.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, orders);
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", number=" + number +
                ", orders=" + orders +
                '}';
    }
    public boolean isAvailableAt(Instant arrival, Instant departure) {
        if (orders == null || orders.isEmpty()) return true;
        for (Order order : orders) {
            TableOrder to = order.getTableOrder();
            if (to != null) {
                boolean overlap = arrival.isBefore(to.getDepartureDatetime()) && departure.isAfter(to.getArrivalDatetime());
                if (overlap) return false;
            }
        }
        return true;
    }

}
