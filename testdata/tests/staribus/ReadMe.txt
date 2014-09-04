These 'stub' XML files are for testing the Staribus Plug and Play system.

They may be written to the 8 EEPROMs in a full system,
i.e. Controller, PrimaryPlugin and Plugins 2-7
using the Core.setModuleConfiguration(module_id, filename) Command.

The getConfiguration() Command will read all EEPROMs
and assemble the individual files into one XML document.
The console window will give a message to show if the document is validated against the schema.

You may also use the DiscoveryTester Instrument in conjunction with a stub Staribus Common Port
to read directly from these files, to test the assembly without any EEPROMs fitted.
To do this edit instruments-common.xml to select the stub streams:

<TxStream>org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StubStaribusTxStream</TxStream>
<RxStream>org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StubStaribusRxStream</RxStream>

Don't forget to restore the original real streams when you are finished:

<TxStream>org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusTxStream</TxStream>
<RxStream>org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.dao.StaribusRxStream</RxStream>

Please report any problems to:
starbase@ukraa.com


L M Newell
2010-04-14