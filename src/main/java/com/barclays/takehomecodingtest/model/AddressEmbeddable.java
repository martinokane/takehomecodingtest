package com.barclays.takehomecodingtest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AddressEmbeddable {

    @Column(name = "address_line1", nullable = false)
    private String line1;

    @Column(name = "address_line2")
    private String line2;

    @Column(name = "address_line3")
    private String line3;

    @Column(name = "town", nullable = false)
    private String town;

    @Column(name = "county", nullable = false)
    private String county;

    @Column(name = "postcode", nullable = false)
    private String postcode;

    public AddressEmbeddable() {
    }

    public AddressEmbeddable(String line1, String line2, String line3, String town, String county, String postcode) {
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.town = town;
        this.county = county;
        this.postcode = postcode;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getLine3() {
        return line3;
    }

    public String getTown() {
        return town;
    }

    public String getCounty() {
        return county;
    }

    public String getPostcode() {
        return postcode;
    }
}
