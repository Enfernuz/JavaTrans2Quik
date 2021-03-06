# JavaTrans2Quik
JNA-based Java-wrapper for Trans2Quik.dll version 1.2 (http://arqatech.com/upload/iblock/006/Trans2QuikAPI_1.2.rar)
("QUIK", Trading Platform by ARQA)

<h3>Как использовать</h3>

1) Инициализируйте экземпляр обёртки библиотеки, используя класс <i>Trans2QuikLibraryLoader</i>:
```
// ...
Trans2QuikLibrary quikAdapter = Trans2QuikLibraryLoader.LIBRARY;
// ...
```
Необязательно заводить отдельную ссылку -- для вызова методов можно обращаться напрямую к <i>Trans2QuikLibraryLoader.LIBRARY</i>.

2) Большинство методов библиотеки Trans2Quik.dll возвращают статус выполнения, а при возникновении ошибки записывают информацию о ней в специальные объекты (обычно это ссылка на код ошибки и буффер под описание ошибки), которые передаются как аргументы метода. Выделим под них место:
```
NativeLongByReference errorCode = new NativeLongByReference();
byte[] buffer = new byte[255];
```
3) Для соединения с терминалом необходимо вызвать метод <i>TRANS2QUIK_CONNECT</i> и, помимо всего прочего, передать туда путь к папке с терминалом QUIK:
```
String pathToQuik = "C:\\Programs\\Quik";
NativeLong returnCode = quikAdapter.TRANS2QUIK_CONNECT(pathToQuik, errorCode, buffer, buffer.length);
```
4) Чтобы отправить транзакцию в терминал, необходимо:

    1) Сформировать строковое представление транзакции (примеры таких представлений можно найти в файле "6 Работа с др приложениями.pdf" из инструкции к API терминала). Чтобы не усложнять, возьмём уже сформированную строку.
    2) Вызвать метод библиотеки <i>TRANS2QUIK_SEND_SYNC_TRANSACTION( ... )</i> и передать ему строковое представление транзации, объекты под код ошибки и сообщение для ошибки.
    <br/>Есть также и асинхронный способ отправки транзакции: с помощью метода <i>TRANS2QUIK_SEND_ASYNC_TRANSACTION</i> и коллбэка <i>TransactionReplyCallback</i>, устанавливаемого посредством метода <i>TRANS2QUIK_SET_TRANSACTIONS_REPLY_CALLBACK</i>.
    
**Внимание!** Строковое представление транзакции взято из руководства к библиотеке trans2quik.dll, и транзакция, основанная на нём, на сервер отправлена не будет, т.к. в ней имеются недействительные значения параметров. Для грамотного составления строк транзакций рекомендую ознакомиться с форматом транзакций в файле "6 Работа с др приложениями.pdf", пункт 6.11.3.
```
String transaction = "ACCOUNT=NL0080000043; CLIENT_CODE=467; TYPE=L; TRANS_ID=1; CLASSCODE=TQBR; SECCODE=RU0008943394; ACTION=NEW_ORDER; OPERATION=S; PRICE=43,21; QUANTITY=3;"
Long result = quikAdapter.TRANS2QUIK_SEND_SYNC_TRANSACTION(transaction, errorCode, buffer, buffer.length);
```
Расшифровка результата <i>result</i> работы метода следующая (алиасы говорят сами за себя):

```
0 = TRANS2QUIK_SUCCESS
1 = TRANS2QUIK_FAILED
2 = TRANS2QUIK_QUIK_TERMINAL_NOT_FOUND
3 = TRANS2QUIK_DLL_VERSION_NOT_SUPPORTED
4 = TRANS2QUIK_ALREADY_CONNECTED_TO_QUIK
5 = TRANS2QUIK_WRONG_SYNTAX
6 = TRANS2QUIK_QUIK_NOT_CONNECTED
7 = TRANS2QUIK_DLL_NOT_CONNECTED
8 = TRANS2QUIK_QUIK_CONNECTED
9 = TRANS2QUIK_QUIK_DISCONNECTED
10 = TRANS2QUIK_DLL_CONNECTED
11 = TRANS2QUIK_DLL_DISCONNECTED
12 = TRANS2QUIK_MEMORY_ALLOCATION_ERROR
13 = TRANS2QUIK_WRONG_CONNECTION_HANDLE
14 = TRANS2QUIK_WRONG_INPUT_PARAMS
```

Подобный подход используется и для вызова других методов.

<h3>Касаемо функций обратного вызова (callbacks)</h3>

Часто терминал после обработки какого-то события может посылать нам информацию. Это может быть, например, обрыв соединения с интернетом или ответ на посланную нами асинхронную транзакцию.
Получение такой информации реализовано через функции обратного вызова: терминал вызывает определённый метод библиотеки с определёнными аргументами, представляющими собой информацию, которую хочет передать терминал.
Обрабатывать эту информацию можно по-разному. Для обработки функций обратного вызова необходимо переопределить их поведение с своём приложении.
Например, мы хотим реагировать на обрыв соединения с Интернетом, логгируя это событие. Для этого в Trans2Quik есть callback под названием _TRANS2QUIK_CONNECTION_STATUS_CALLBACK_. 
В обёртке он представлен интерфейсом ConnectionStatusCallback, наследником общего интерфейса коллбэков _StdCallCallback_.
Каждый коллбэк имеет у себя метод _void callback(...)_ со своим набором аргументов.
В своём коде создаём объект интерфейса ConnectionStatusCallback и переопределяем у него метод callback:

```
Trans2QuikLibrary.ConnectionStatusCallback connectionStatusCallback = new Trans2QuikLibrary.ConnectionStatusCallback() {
        @Override
        public void callback(
            NativeLong nConnectionEvent, 
            NativeLong nExtendedErrorCode, 
            String lpcstrInfoMessage
        ) {
            Logger.getLogger(this.getClass().getName()).info(String.format("[%s]: nConnectionEvent = %d, nExtendedErrorCode = %d, lpcstrInfoMessage = %s", "ConnectionStatusCallback", nConnectionEvent.intValue(), nExtendedErrorCode.intValue(), lpcstrInfoMessage));
            ...
        }
    };
```

Другой пример: реагирование на событие изменения статуса заявки

```
Trans2QuikLibrary.OrderStatusCallback orderStatusCallback = new Trans2QuikLibrary.OrderStatusCallback() {
        @Override
        public void callback(
                            NativeLong nMode, 
                            int dwTransID, 
                            double dNumber,
                            String ClassCode, 
                            String SecCode, 
                            double dPrice,
                            NativeLong nBalance, 
                            double dValue, 
                            NativeLong nIsSell, 
                            NativeLong nStatus,
                            NativeLong nOrderDescriptor) {
            Logger.getLogger(this.getClass().getName()).info(String.format("[%s]: nMode = %d, dwTransID = %d, dNumber = %f, ClassCode = %s, SecCode = %s, dPrice = %f, nBalance = %d, dValue = %f, nIsSell = %d, nStatus = %d, nOrderDescriptor = %d",
                    "OrderStatusCallback", nMode.intValue(), dwTransID, dNumber, ClassCode, SecCode, dPrice, nBalance.intValue(), dValue, nIsSell.intValue(), nStatus.intValue(), nOrderDescriptor.intValue()));
        }
    };
```

Разумеется, логгирование -- не единственная возможная ваша реакция на событие. 
Например, информацию _OrderStatusCallback_ можно использовать для контролирования работы бота.

Экземпляры обработчиков коллбэков нужно передать библиотеке Trans2Quik.dll. Для каждого коллбэка в библиотеке существует свой метод для этого. Например, для установки коллбэка на статус соединения (_ConnectionStatusCallback_) используется метод <i>TRANS2QUIK_SET_CONNECTION_STATUS_CALLBACK</i>:

```
retcode = quikAdapter.TRANS2QUIK_SET_CONNECTION_STATUS_CALLBACK(connectionStatusCallback, errorCode, buffer, buffer.length;
```

Вообще говоря, реализовывать и устанавливать коллбэки -- дело сугубо личное, определяемое исходя из списка событий, которые вам интересны.

Подводя итог: результаты некоторых событий, происходящих в терминале, -- обработки транзакций, изменения статуса заявок и многое другое (см. "6 Работа с др приложениями.pdf") -- получаются посредством коллбэков.

<h3>Как получать информацию из Quik в Java?</h3>

Я знаю как минимум два способа:

1. Установить на стороне QUIK RPC-сервис (сервис вызова удалённых процедур): <br/>
    https://github.com/Enfernuz/quik-lua-rpc

    Java-клиент к нему: https://github.com/Enfernuz/quik-lua-rpc-java-client
    
    Реализует все методы API QLua, поэтому умеет также и в отправку транзакций (метод QLua `sendTransaction`). В сущности, при использовании `quik-lua-rpc` вам уже не нужен `JavaTrans2Quik`.

2. Ещё один RPC-сервис. Проще в установке, но менее функционален: https://github.com/boscogh/quik_pipe
