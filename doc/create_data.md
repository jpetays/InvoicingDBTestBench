## Create data

#### customer
> INSERT INTO warehouse.customer (id, name, address) VALUES ($customerIndex, $name, $streetAddress)    
> CREATE (a:customer {customerId: $customerIndex, name: $name, address: $streetAddress})

#### invoice
if (j < sequentialInvoices) {  
if (invoiceIndex == firstInvoice) {
>     INSERT INTO warehouse.invoice (id, customerId, state, duedate, previousinvoice) VALUES ($invoiceIndex, $customerIndex, $state,STR_TO_DATE('$dueDateAsString','%d-%m-%Y'), $invoiceIndex)  
>     CREATE (l:invoice {invoiceId: $invoiceIndex, customerId: $customerIndex, state: $state, duedate: date({ year: $year, month: $month, day: $day }), previousinvoice: $invoiceIndex})  
>     MATCH (a:customer),(l:invoice) WHERE a.customerId = $customerIndex AND l.invoiceId = $invoiceIndex CREATE (a)-[m:PAYS]->(l)  

else

>     INSERT INTO warehouse.invoice (id, customerId, state, duedate, previousinvoice) VALUES ($invoiceIndex, $customerIndex, $state,STR_TO_DATE('$dueDateAsString','%d-%m-%Y'), $(invoiceIndex - 1))  
>     CREATE (l:invoice {invoiceId: $invoiceIndex, customerId: $customerIndex, state: $state, duedate: date({ year: $year, month: $month, day: $day }), previousinvoice: $(invoiceIndex - 1)})  
>     MATCH (a:customer),(l:invoice) WHERE a.customerId = $customerIndex AND l.invoiceId = $invoiceIndex CREATE (a)-[m:PAYS]->(l)  
>     MATCH (a:invoice),(b:invoice) WHERE a.invoiceId = $(invoiceIndex - 1) AND b.invoiceId = $invoiceIndex CREATE (a)-[m:PREVIOUS_INVOICE]->(b)  

end else

>   INSERT INTO warehouse.invoice (id, customerId, state, duedate, previousinvoice) VALUES ($invoiceIndex, $customerIndex, $state,STR_TO_DATE('$dueDateAsString','%d-%m-%Y'), $invoiceIndex)  
>   CREATE (l:invoice {invoiceId: $invoiceIndex, customerId: $customerIndex, state: $state, duedate: date({ year: $year, month: $month, day: $day }), previousinvoice: $invoiceIndex})  
>   MATCH (a:customer),(l:invoice) WHERE a.customerId = $customerIndex AND l.invoiceId = $invoiceIndex CREATE (a)-[m:PAYS]->(l)

end

#### target
> INSERT INTO warehouse.target (id, name, address, customerid) VALUES ($targetIndex, $name, $streetAddress, $customerIndex)  
> CREATE (t:target {targetId: $targetIndex, name: $name, address: $streetAddress, customerid: $customerIndex })  
> MATCH (c:customer),(t:target) WHERE c.customerId = $customerIndex AND t.targetId = $targetIndex CREATE (c)-[ct:CUSTOMER_TARGET]->(t)

#### worktarget
> INSERT INTO warehouse.worktarget (workId, targetId) VALUES ($workIndex, $targetIndex)    
> MATCH (w:work),(t:target) WHERE w.workId = $workIndex  AND t.targetId = $targetIndex CREATE (w)-[wt:WORK_TARGET]->(t)  
> MATCH (t:target),(w:work) WHERE t.targetId = $targetIndex AND w.workId = $workIndex CREATE (t)-[wt:WORK_TARGET]->(w)

#### workinvoice
> INSERT INTO warehouse.workinvoice (workId, invoiceId) VALUES ($workIndex, $invoiceIndex)  
> MATCH (w:work),(i:invoice) WHERE w.workId = $workIndex AND i.invoiceId = $invoiceIndex CREATE (w)-[wi:WORK_INVOICE]->(i)  
> MATCH (i:invoice),(w:work) WHERE i.invoiceId = $invoiceIndex AND w.workId = $workIndex CREATE (i)-[wi:WORK_INVOICE]->(w)

### item
> INSERT INTO warehouse.item (id, name, balance, unit, purchaseprice, vat, removed) VALUES (?,?,?,?,?,?,?)  
> CREATE (v:item {itemId: $itemIndex, name: $itemName, balance: $balance, unit:m, purchaseprice: $purchaseprice, vat: $vat, removed: $removed})  
> CREATE (v:item {itemId: $itemIndex, name: $itemName, balance: $balance, unit:pcs, purchaseprice: $purchaseprice, vat: $vat, removed: $removed})

### worktype
> INSERT INTO warehouse.worktype (id, name, price) VALUES (?, ?, ?)  
> CREATE (wt:worktype {worktypeId: $workTypeIndex, name:design, price: $price})  
> CREATE (wt:worktype {worktypeId: $workTypeIndex, name:work, price: $price})  
> CREATE (wt:worktype {worktypeId: $workTypeIndex, name:supporting work, price: $price})

### invoice
> INSERT INTO warehouse.invoice (id, customerId, state, duedate, previousinvoice) VALUES (?,?,?,?,?)

if (invoiceIndex == firstInvoiceIndex) {

> INSERT INTO warehouse.invoice (id, customerId, state, duedate, previousinvoice) VALUES ($invoiceIndex, $customerIndex, $state,STR_TO_DATE('$dueDateAsString','%d-%m-%Y'), $invoiceIndex)  
> CREATE (l:invoice {invoiceId: $invoiceIndex, customerId: $customerIndex, state: $state, duedate: date({ year: $year, month: $month, day: $day }), previousinvoice: $invoiceIndex})  
> MATCH (a:customer),(l:invoice) WHERE a.customerId = $customerIndex AND l.invoiceId = $invoiceIndex CREATE (a)-[m:PAYS]->(l)

else

> INSERT INTO warehouse.invoice (id, customerId, state, duedate, previousinvoice) VALUES ($invoiceIndex, $customerIndex, $state,STR_TO_DATE('$dueDateAsString','%d-%m-%Y'), $(invoiceIndex - 1))  
> CREATE (l:invoice {invoiceId: $invoiceIndex, customerId: $customerIndex, state: $state, duedate: date({ year: $year, month: $month, day: $day }), previousinvoice: $(invoiceIndex - 1)})  
> MATCH (a:customer),(l:invoice) WHERE a.customerId = $customerIndex AND l.invoiceId = $invoiceIndex CREATE (a)-[m:PAYS]->(l)  
> MATCH (a:invoice),(b:invoice) WHERE a.invoiceId = $(invoiceIndex - 1) AND b.invoiceId = $invoiceIndex CREATE (a)-[m:PREVIOUS_INVOICE]->(b)

end