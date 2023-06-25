# Czytnik Polskich Banknotów

Czytnik Polskich Banknotów is an Open Source mobile app, which goal is to help visually handicapped people live the life we all do. By detecting our local currency's denomination (PLN - Polish zloty) using neural networks, it allows a user to know what banknote they're holding. The application provides support on a daily basis, such as going shopping, using ATMs, buying tickets or visiting restaurants.

## Download

Aviable at [Play Store](https://play.google.com/store/apps/details?id=pg.eti.project.polishbanknotes)

Not currently aviable at App Store.

## About technologies

Short description of technologies used in this project.

### Mobile app

We're developing two versions of this application - one for Android and one for iOS. Both of them are written natively using corresponding technologies - Kotlin for Android and Swift for iOS. Currently, supported system versions are:

* iOS 11.0+ (iPhone 5s+)
* Android 7+

### Neural network

The neural network we're working on is based on TensorFlow+Keras Image classification. We used yolo architecture to train our best model - created by Team Android.

### Datasets

We're using datasets created by previous teams working on this subject (information about those are given in *Acknowledgments* section). About numbers, we currently have around 3500-4800 images per denomination, mostly in good quality with 0 or 1 banknote at it.

## Help

For help, please contact authors of this project or create an issue on GitHub.

## Authors

### Team iOS

* [Jakub Dajczak](https://github.com/qaziok)
* [Miłosz Chojnacki](https://github.com/Buzeqq)
* [Anton Delinac](https://github.com/anton-0)
* [Stanisław Smykowski](https://github.com/StanislawSm)
* [Mateusz Sowiński](https://github.com/wichurax)

### Team Android

* [Krzysztof Jędraszek](https://github.com/kjedrasz2137)
* [Jakub Szymański](https://github.com/Corax0x01)
* [Filip Żemajtys](https://github.com/fzemi)
* [Jan Kornacki](https://github.com/jankejc)
* [Mikołaj Wirkijowski](https://github.com/mikolajwirkijowski97)

## Acknowledgments

### Supervisors

[Jan Cychnerski](https://github.com/jachoo)

### Previous teams working on this subject

#### [Polish Banknote Recognition Application](https://github.com/bartekkkkk/PG2020)

- Jacek Ardanowski
- Bartłomiej Gawrych
- Adam Grabowski
- Kamil Jabłoński
- Bartosz Kuncer

#### [Vison Project](https://github.com/theATM/Vison-Project-Repository)

- Aleksander Madajczak
- Kamil Pokornicki
- Karol Dziki

### Datasets

We were using datasets created by previous teams working on this subject.

[Polish banknotes / Polskie banknoty](https://www.kaggle.com/datasets/bartomiejgawrych/polish-banknotes-polskie-banknoty)

[VisonData PLN banknotes 2021](https://www.kaggle.com/datasets/99636797e98397fa6161113867b50bed663be07900885852cda713b1cc76e52d)

### Training resources

Thanks to kindness of Ph.D. Jacek Rumiński from Biomedical Engineering Department, we were able to use DGX Station, which is a powerful machine learning server, to train our neural network.

This work has been partially supported by Statutory Funds of Electronics, Telecommunications and Informatics Faculty, Gdańsk University of Technology.

## License

This project is licensed under the GNU General Public License v3.0 - see the LICENSE file for details