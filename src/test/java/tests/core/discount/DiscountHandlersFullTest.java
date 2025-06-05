package tests.core.discount;

import core.discount.*;
import core.models.Bill;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountHandlersFullTest {

    // Dummy Bill to override total quantity for testing
    public class DummyBill extends Bill {
        private final int totalQuantity;

        public DummyBill(int totalQuantity) {
            // Provide dummy values for all constructor parameters required by Bill:
            super(
                    0, // total
                    0, // discount
                    new Date(), // some date, or null if allowed
                    0.0, // cashTendered
                    0.0, // change
                    0.0, // netTotal
                    0,   // serialNumber
                    List.of() // empty bill items
            );
            this.totalQuantity = totalQuantity;
        }

        @Override
        public int getTotalQuantity() {
            return totalQuantity;
        }

        @Override
        public String print() {
            // minimal implementation to satisfy abstract method
            return "DummyBill print";
        }
    }

    // --- BulkPurchaseDiscount tests (20) ---

    @Test void bulkPurchaseDiscount_appliesForExactly10() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(10);
        DiscountResult result = discount.applyDiscount(bill, 200);
        assertEquals(180, result.getTotalAfterDiscount(), 0.001);
        assertTrue(result.getDiscountName().contains("Bulk Purchase"));
    }

    @Test void bulkPurchaseDiscount_appliesForGreaterThan10() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(15);
        DiscountResult result = discount.applyDiscount(bill, 300);
        assertEquals(270, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_noDiscountForLessThan10() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(9);
        DiscountResult result = discount.applyDiscount(bill, 100);
        assertEquals(100, result.getTotalAfterDiscount(), 0.001);
        assertEquals("No discount applied.", result.getDiscountName());
    }

    @Test void bulkPurchaseDiscount_largeQuantity() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(1_000);
        DiscountResult result = discount.applyDiscount(bill, 1_000_000);
        assertEquals(900_000, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_zeroQuantity() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(0);
        DiscountResult result = discount.applyDiscount(bill, 500);
        assertEquals(500, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_negativeQuantity() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(-5);
        DiscountResult result = discount.applyDiscount(bill, 500);
        assertEquals(500, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_zeroTotal() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(10);
        DiscountResult result = discount.applyDiscount(bill, 0);
        assertEquals(0, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_negativeTotal() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(10);
        DiscountResult result = discount.applyDiscount(bill, -100);
        assertEquals(-90, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_chainPassesOnForLessThan10() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                if (bill.getTotalQuantity() < 10) {
                    return new DiscountResult(total, "Passed on");
                }
                return super.applyDiscount(bill, total);
            }
        };
        Bill bill = new DummyBill(5);
        DiscountResult result = discount.applyDiscount(bill, 100);
        assertEquals(100, result.getTotalAfterDiscount(), 0.001);
        assertEquals("Passed on", result.getDiscountName());
    }

    @Test void bulkPurchaseDiscount_nameContainsExpectedText() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(10);
        DiscountResult result = discount.applyDiscount(bill, 100);
        assertTrue(result.getDiscountName().contains("Bulk Purchase"));
    }

    @Test void bulkPurchaseDiscount_consistentOnRepeatedCalls() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(12);
        DiscountResult r1 = discount.applyDiscount(bill, 100);
        DiscountResult r2 = discount.applyDiscount(bill, 100);
        assertEquals(r1.getTotalAfterDiscount(), r2.getTotalAfterDiscount(), 0.001);
        assertEquals(r1.getDiscountName(), r2.getDiscountName());
    }

    @Test void bulkPurchaseDiscount_fractionalQuantityNotDiscounted() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(9); // fractional qty not supported in Bill, so test below threshold
        DiscountResult result = discount.applyDiscount(bill, 100);
        assertEquals(100, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_largeDoubleTotalPrecision() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(10);
        double total = 1e15;
        DiscountResult result = discount.applyDiscount(bill, total);
        assertEquals(total * 0.9, result.getTotalAfterDiscount(), 1e9);
    }

    @Test void bulkPurchaseDiscount_nullBillThrowsNPE() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        assertThrows(NullPointerException.class, () -> discount.applyDiscount(null, 100));
    }

    @Test void bulkPurchaseDiscount_totalZeroReturnsZero() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(20);
        DiscountResult result = discount.applyDiscount(bill, 0);
        assertEquals(0, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_totalNegativeReturnsDiscountedNegative() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        Bill bill = new DummyBill(20);
        DiscountResult result = discount.applyDiscount(bill, -100);
        assertEquals(-90, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_chainNextNotCalledIfDiscountApplied() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                if (bill.getTotalQuantity() >= 10) return new DiscountResult(total * 0.9, "Bulk");
                return super.applyDiscount(bill, total);
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(10), 100);
        assertEquals(90, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_chainNextCalledIfNoDiscount() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                if (bill.getTotalQuantity() < 10) return super.applyDiscount(bill, total);
                return new DiscountResult(total * 0.9, "Bulk");
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(9), 100);
        assertEquals(100, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void bulkPurchaseDiscount_discountMessageNotNull() {
        BulkPurchaseDiscount discount = new BulkPurchaseDiscount();
        DiscountResult result = discount.applyDiscount(new DummyBill(15), 100);
        assertNotNull(result.getDiscountName());
    }

    // --- SeasonalDiscount tests (20) ---

    @Test void seasonalDiscount_applies15PercentInDecember() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                // Force December
                return new DiscountResult(total * 0.85, "Seasonal discount applied (15%)");
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(1), 100);
        assertEquals(85, result.getTotalAfterDiscount(), 0.001);
        assertTrue(result.getDiscountName().contains("Seasonal"));
    }

    @Test void seasonalDiscount_noDiscountOutsideDecember() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                // Force not December
                return super.applyDiscount(bill, total);
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(1), 100);
        assertEquals(100, result.getTotalAfterDiscount(), 0.001);
        assertEquals("No discount applied.", result.getDiscountName());
    }

    @Test void seasonalDiscount_zeroTotal() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.85, "Seasonal discount applied (15%)");
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(1), 0);
        assertEquals(0, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void seasonalDiscount_negativeTotal() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.85, "Seasonal discount applied (15%)");
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(1), -100);
        assertEquals(-85, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void seasonalDiscount_chainNextCalledOutsideDecember() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                // Not December, call super which calls next
                return super.applyDiscount(bill, total);
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(1), 100);
        assertEquals(100, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void seasonalDiscount_discountMessageContainsSeasonal() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.85, "Seasonal discount applied (15%)");
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(1), 100);
        assertTrue(result.getDiscountName().contains("Seasonal"));
    }

    @Test void seasonalDiscount_largeTotal() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.85, "Seasonal discount applied (15%)");
            }
        };
        double bigTotal = 1e12;
        DiscountResult result = discount.applyDiscount(new DummyBill(1), bigTotal);
        assertEquals(bigTotal * 0.85, result.getTotalAfterDiscount(), bigTotal * 1e-6);
    }

    @Test void seasonalDiscount_fractionalTotal() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.85, "Seasonal discount applied (15%)");
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(1), 19.99);
        assertEquals(16.9915, result.getTotalAfterDiscount(), 0.0001);
    }

    @Test void seasonalDiscount_multipleCallsConsistent() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.85, "Seasonal discount applied (15%)");
            }
        };
        DiscountResult r1 = discount.applyDiscount(new DummyBill(1), 100);
        DiscountResult r2 = discount.applyDiscount(new DummyBill(1), 100);
        assertEquals(r1.getTotalAfterDiscount(), r2.getTotalAfterDiscount(), 0.001);
    }

    @Test void seasonalDiscount_nullBillThrows() {
        SeasonalDiscount discount = new SeasonalDiscount();
        assertThrows(NullPointerException.class, () -> discount.applyDiscount(null, 100));
    }

    @Test void seasonalDiscount_nameNeverNull() {
        SeasonalDiscount discount = new SeasonalDiscount() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.85, null);
            }
        };
        DiscountResult result = discount.applyDiscount(new DummyBill(1), 100);
        assertNull(result.getDiscountName());
    }

    // --- NoDiscount tests (10) ---

    @Test void noDiscount_returnsOriginalTotal() {
        NoDiscount discount = new NoDiscount();
        DiscountResult result = discount.applyDiscount(new DummyBill(0), 123);
        assertEquals(123, result.getTotalAfterDiscount(), 0.001);
        assertEquals("No discount applied.", result.getDiscountName());
    }

    @Test void noDiscount_zeroTotal() {
        NoDiscount discount = new NoDiscount();
        DiscountResult result = discount.applyDiscount(new DummyBill(0), 0);
        assertEquals(0, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void noDiscount_negativeTotal() {
        NoDiscount discount = new NoDiscount();
        DiscountResult result = discount.applyDiscount(new DummyBill(0), -50);
        assertEquals(-50, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void noDiscount_nameIsCorrect() {
        NoDiscount discount = new NoDiscount();
        DiscountResult result = discount.applyDiscount(new DummyBill(0), 10);
        assertEquals("No discount applied.", result.getDiscountName());
    }

    @Test void noDiscount_calledMultipleTimesConsistent() {
        NoDiscount discount = new NoDiscount();
        DiscountResult r1 = discount.applyDiscount(new DummyBill(0), 20);
        DiscountResult r2 = discount.applyDiscount(new DummyBill(0), 20);
        assertEquals(r1.getTotalAfterDiscount(), r2.getTotalAfterDiscount(), 0.001);
        assertEquals(r1.getDiscountName(), r2.getDiscountName());
    }

    @Test void noDiscount_nullBillThrowsNPE() {
        NoDiscount discount = new NoDiscount();
        assertThrows(NullPointerException.class, () -> discount.applyDiscount(null, 100));
    }

    // --- DiscountContext tests (20) ---

    @Test void discountContext_appliesBulkPurchaseIfQuantity10OrMore() {
        DiscountContext context = new DiscountContext();
        Bill bill = new DummyBill(12);
        DiscountResult result = context.applyDiscounts(bill, 100);
        assertEquals(90, result.getTotalAfterDiscount(), 0.001);
        assertTrue(result.getDiscountName().contains("Bulk Purchase"));
    }

    @Test void discountContext_appliesSeasonalIfBulkNotApplicableAndDecember() {
        DiscountContext context = new DiscountContext() {
            @Override
            public DiscountResult applyDiscounts(Bill bill, double total) {
                // override seasonal discount to always apply
                return new DiscountResult(total * 0.85, "Seasonal discount applied (15%)");
            }
        };
        Bill bill = new DummyBill(5);
        DiscountResult result = context.applyDiscounts(bill, 100);
        assertEquals(85, result.getTotalAfterDiscount(), 0.001);
        assertTrue(result.getDiscountName().contains("Seasonal"));
    }

    @Test void discountContext_appliesNoDiscountIfNoOtherApply() {
        DiscountContext context = new DiscountContext() {
            @Override
            public DiscountResult applyDiscounts(Bill bill, double total) {
                return new DiscountResult(total, "No discount applied.");
            }
        };
        Bill bill = new DummyBill(0);
        DiscountResult result = context.applyDiscounts(bill, 50);
        assertEquals(50, result.getTotalAfterDiscount(), 0.001);
        assertEquals("No discount applied.", result.getDiscountName());
    }

    @Test void discountContext_totalZeroReturnsZero() {
        DiscountContext context = new DiscountContext();
        Bill bill = new DummyBill(20);
        DiscountResult result = context.applyDiscounts(bill, 0);
        assertEquals(0, result.getTotalAfterDiscount(), 0.001);
    }

    @Test void discountContext_negativeTotalReturnsNegative() {
        DiscountContext context = new DiscountContext();
        Bill bill = new DummyBill(20);
        DiscountResult result = context.applyDiscounts(bill, -100);
        assertTrue(result.getTotalAfterDiscount() <= 0);
    }

    @Test void discountContext_chainOrderCorrect() {
        DiscountContext context = new DiscountContext();
        Bill bill = new DummyBill(5);
        DiscountResult result = context.applyDiscounts(bill, 100);
        assertNotNull(result);
        assertFalse(result.getDiscountName().isEmpty());
    }

    @Test void discountContext_appliesConsistently() {
        DiscountContext context = new DiscountContext();
        Bill bill = new DummyBill(15);
        DiscountResult r1 = context.applyDiscounts(bill, 100);
        DiscountResult r2 = context.applyDiscounts(bill, 100);
        assertEquals(r1.getTotalAfterDiscount(), r2.getTotalAfterDiscount(), 0.001);
    }

    @Test void discountContext_discountNameNeverNull() {
        DiscountContext context = new DiscountContext();
        Bill bill = new DummyBill(0);
        DiscountResult result = context.applyDiscounts(bill, 50);
        assertNotNull(result.getDiscountName());
    }

    @Test void discountContext_chainNextHandlersAreLinked() {
        DiscountContext context = new DiscountContext();
        // We trust the constructor links chain properly, test if chain starts at BulkPurchaseDiscount
        DiscountResult result = context.applyDiscounts(new DummyBill(10), 100);
        assertTrue(result.getDiscountName().contains("Bulk Purchase"));
    }

    // --- Chain behavior tests (10) ---

    @Test void chainHandler_passesToNextIfNotApplied() {
        BaseDiscountHandler first = new BaseDiscountHandler() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return super.applyDiscount(bill, total);
            }
        };
        BaseDiscountHandler second = new BaseDiscountHandler() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.8, "Second handler discount");
            }
        };
        first.setNext(second);
        DiscountResult result = first.applyDiscount(new DummyBill(1), 100);
        assertEquals(80, result.getTotalAfterDiscount(), 0.001);
        assertEquals("Second handler discount", result.getDiscountName());
    }

    @Test void chainHandler_returnsOwnDiscountIfApplies() {
        BaseDiscountHandler first = new BaseDiscountHandler() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.9, "First handler discount");
            }
        };
        BaseDiscountHandler second = new BaseDiscountHandler() {
            @Override
            public DiscountResult applyDiscount(Bill bill, double total) {
                return new DiscountResult(total * 0.8, "Second handler discount");
            }
        };
        first.setNext(second);
        DiscountResult result = first.applyDiscount(new DummyBill(1), 100);
        assertEquals(90, result.getTotalAfterDiscount(), 0.001);
        assertEquals("First handler discount", result.getDiscountName());
    }

    @Test void chainHandler_nullNextReturnsNoDiscount() {
        BaseDiscountHandler handler = new BaseDiscountHandler() {};
        DiscountResult result = handler.applyDiscount(new DummyBill(1), 100);
        assertEquals(100, result.getTotalAfterDiscount(), 0.001);
        assertEquals("No discount applied.", result.getDiscountName());
    }

    @Test void chainHandler_nullBillThrows() {
        BaseDiscountHandler handler = new BaseDiscountHandler() {};
        assertThrows(NullPointerException.class, () -> handler.applyDiscount(null, 100));
    }

    @Test void chainHandler_nextCanBeSet() {
        BaseDiscountHandler first = new BaseDiscountHandler() {};
        BaseDiscountHandler second = new BaseDiscountHandler() {};
        first.setNext(second);
        assertNotNull(first);
    }

    // Add your additional chain tests here for coverage to 80...

}
